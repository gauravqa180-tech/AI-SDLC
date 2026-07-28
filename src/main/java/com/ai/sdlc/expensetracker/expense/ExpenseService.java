package com.ai.sdlc.expensetracker.expense;

import com.ai.sdlc.expensetracker.category.Category;
import com.ai.sdlc.expensetracker.category.CategoryRepository;
import com.ai.sdlc.expensetracker.common.NotFoundException;
import com.ai.sdlc.expensetracker.expense.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expense.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.dto.ExpenseUpdateRequest;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request) {
        Category category = requireActiveCategory(request.categoryId());

        Expense e = new Expense();
        e.setAmount(request.amount());
        e.setExpenseDate(request.expenseDate());
        e.setNote(trimToNull(request.note()));
        e.setCategory(category);

        return toResponse(expenseRepository.save(e));
    }

    public ExpenseResponse get(long id) {
        return expenseRepository.findById(id)
                .map(ExpenseService::toResponse)
                .orElseThrow(() -> new NotFoundException("expense not found: " + id));
    }

    @Transactional
    public ExpenseResponse update(long id, ExpenseUpdateRequest request) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("expense not found: " + id));

        Category category = requireActiveCategory(request.categoryId());

        e.setAmount(request.amount());
        e.setExpenseDate(request.expenseDate());
        e.setNote(trimToNull(request.note()));
        e.setCategory(category);

        return toResponse(expenseRepository.save(e));
    }

    @Transactional
    public void delete(long id) {
        if (!expenseRepository.existsById(id)) {
            throw new NotFoundException("expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    public List<ExpenseResponse> list(LocalDate from, LocalDate to, Long categoryId, BigDecimal minAmount, BigDecimal maxAmount, String q, Sort sort) {
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to))
                .and(ExpenseSpecifications.categoryId(categoryId))
                .and(ExpenseSpecifications.amountMin(minAmount))
                .and(ExpenseSpecifications.amountMax(maxAmount))
                .and(ExpenseSpecifications.noteContains(q));

        return expenseRepository.findAll(spec, sort).stream()
                .map(ExpenseService::toResponse)
                .toList();
    }

    public BigDecimal monthlyTotal(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();
        return expenseRepository.sumAmountBetween(from, to);
    }

    private Category requireActiveCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("category not found: " + categoryId));
        if (!category.isActive()) {
            throw new IllegalArgumentException("category is disabled");
        }
        return category;
    }

    private static ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getExpenseDate(),
                e.getCategory().getId(),
                e.getCategory().getName(),
                e.getNote(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
