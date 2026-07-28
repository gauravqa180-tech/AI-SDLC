package com.example.expensetracker.expense.service;

import com.example.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.example.expensetracker.expense.api.dto.MonthlyTotalResponse;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req) {
        Expense e = new Expense();
        e.setAmount(req.amount());
        e.setExpenseDate(req.date());
        e.setCategory(req.category().trim());
        e.setNote(req.note());
        return toResponse(expenseRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(LocalDate startDate, LocalDate endDate, String category, String sortBy, String sortDir) {
        Specification<Expense> spec = Specification.where(null);
        if (startDate != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("expenseDate"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("expenseDate"), endDate));
        }
        if (category != null && !category.isBlank()) {
            String normalized = category.trim();
            spec = spec.and((root, q, cb) -> cb.equal(root.get("category"), normalized));
        }

        Sort sort = toSort(sortBy, sortDir);
        return expenseRepository.findAll(spec, sort).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Expense not found"));
        return toResponse(e);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest req) {
        try {
            Expense e = expenseRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Expense not found"));

            // optimistic lock: ensure client edits the latest version
            if (!e.getVersion().equals(req.version())) {
                throw new ResponseStatusException(CONFLICT, "Expense was modified by another session. Please refresh and try again.");
            }

            e.setAmount(req.amount());
            e.setExpenseDate(req.date());
            e.setCategory(req.category().trim());
            e.setNote(req.note());

            Expense saved = expenseRepository.saveAndFlush(e);
            return toResponse(saved);
        } catch (OptimisticLockingFailureException | OptimisticLockException ex) {
            throw new ResponseStatusException(CONFLICT, "Expense was modified by another session. Please refresh and try again.");
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Expense not found");
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MonthlyTotalResponse monthlyTotal(YearMonth month) {
        YearMonth m = (month == null) ? YearMonth.now() : month;
        LocalDate start = m.atDay(1);
        LocalDate end = m.atEndOfMonth();
        BigDecimal total = expenseRepository.sumAmountBetween(start, end);
        return new MonthlyTotalResponse(m, total);
    }

    private Sort toSort(String sortBy, String sortDir) {
        String by = (sortBy == null || sortBy.isBlank()) ? "date" : sortBy;
        String dir = (sortDir == null || sortDir.isBlank()) ? "desc" : sortDir;

        String property = switch (by.toLowerCase()) {
            case "amount" -> "amount";
            case "date" -> "expenseDate";
            default -> "expenseDate";
        };
        Sort.Direction direction = dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, property).and(Sort.by(Sort.Direction.DESC, "id"));
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getExpenseDate(),
                e.getCategory(),
                e.getNote(),
                e.getVersion(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
