package com.example.expensetracker.expenses;

import com.example.expensetracker.budgets.BudgetService;
import com.example.expensetracker.budgets.dto.BudgetStatusDto;
import com.example.expensetracker.categories.Category;
import com.example.expensetracker.categories.CategoryService;
import com.example.expensetracker.expenses.dto.ExpenseDto;
import com.example.expensetracker.expenses.dto.ExpenseUpsertRequest;
import com.example.expensetracker.expenses.dto.MonthlySummaryDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static com.example.expensetracker.expenses.ExpenseSpecifications.*;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryService categoryService;
    private final BudgetService budgetService;

    @Value("${app.expenses.undo-window-seconds:30}")
    private long undoWindowSeconds;

    @Transactional
    public ExpenseDto create(ExpenseUpsertRequest req) {
        Category c = categoryService.requireCategory(req.categoryId());
        validateDate(req.date());

        Expense e = new Expense();
        e.setAmount(req.amount());
        e.setExpenseDate(req.date());
        e.setCategory(c);
        e.setNote(req.note());
        Expense saved = expenseRepository.save(e);
        return toDto(saved);
    }

    @Transactional
    public ExpenseDto update(long id, ExpenseUpsertRequest req) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
        if (e.getDeletedAt() != null) {
            throw new IllegalArgumentException("Cannot edit a deleted expense");
        }
        Category c = categoryService.requireCategory(req.categoryId());
        validateDate(req.date());

        e.setAmount(req.amount());
        e.setExpenseDate(req.date());
        e.setCategory(c);
        e.setNote(req.note());
        return toDto(expenseRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto> list(Optional<String> q, Optional<Long> categoryId, Optional<LocalDate> from, Optional<LocalDate> to,
                                Optional<YearMonth> month, Optional<String> sortBy, Optional<String> sortDir) {

        LocalDate fromDate = from.orElse(null);
        LocalDate toDate = to.orElse(null);
        if (month.isPresent()) {
            YearMonth ym = month.get();
            fromDate = ym.atDay(1);
            toDate = ym.atEndOfMonth();
        }

        Specification<Expense> spec = Specification.where(notDeleted())
                .and(categoryIdEquals(categoryId.orElse(null)))
                .and(dateBetween(fromDate, toDate))
                .and(noteContainsIgnoreCase(q.orElse(null)));

        Sort sort = toSort(sortBy.orElse("date"), sortDir.orElse("desc"));
        return expenseRepository.findAll(spec, sort).stream().map(ExpenseService::toDto).toList();
    }

    @Transactional
    public void softDelete(long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
        if (e.getDeletedAt() == null) {
            e.setDeletedAt(Instant.now());
            expenseRepository.save(e);
        }
    }

    @Transactional
    public ExpenseDto restore(long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
        if (e.getDeletedAt() == null) {
            return toDto(e);
        }
        Instant deletedAt = e.getDeletedAt();
        if (deletedAt.plusSeconds(undoWindowSeconds).isBefore(Instant.now())) {
            throw new IllegalArgumentException("Undo window expired");
        }
        e.setDeletedAt(null);
        return toDto(expenseRepository.save(e));
    }

    @Transactional(readOnly = true)
    public MonthlySummaryDto monthlySummary(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        BigDecimal total = expenseRepository.sumBetween(from, to);
        return new MonthlySummaryDto(month, total);
    }

    @Transactional(readOnly = true)
    public List<BudgetService.BudgetAlert> budgetAlertsForMonth(YearMonth month) {
        return budgetService.evaluateAlerts(month);
    }

    private void validateDate(LocalDate date) {
        if (date.isAfter(LocalDate.now().plusDays(1))) {
            throw new IllegalArgumentException("Expense date cannot be in the far future");
        }
    }

    private Sort toSort(String sortBy, String sortDir) {
        String prop;
        switch (sortBy.toLowerCase()) {
            case "amount" -> prop = "amount";
            case "category" -> prop = "category.name";
            case "date" -> prop = "expenseDate";
            default -> throw new IllegalArgumentException("Unsupported sortBy: " + sortBy);
        }
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, prop);
    }

    static ExpenseDto toDto(Expense e) {
        return new ExpenseDto(
                e.getId(),
                e.getAmount(),
                e.getExpenseDate(),
                e.getCategory().getId(),
                e.getCategory().getName(),
                e.getNote(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getDeletedAt()
        );
    }

    public record CreateExpenseResponse(ExpenseDto expense, List<BudgetStatusDto> budgetAlerts) {}
}
