package com.example.expensetracker.expense.service;

import com.example.expensetracker.common.api.NotFoundException;
import com.example.expensetracker.expense.api.dto.ExpenseRequest;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public Expense create(ExpenseRequest request) {
        Expense e = Expense.builder()
                .amount(request.amount())
                .date(request.date())
                .category(request.category().trim())
                .note(request.note())
                .build();
        return expenseRepository.save(e);
    }

    @Transactional
    public Expense update(Long id, ExpenseRequest request) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

        existing.setAmount(request.amount());
        existing.setDate(request.date());
        existing.setCategory(request.category().trim());
        existing.setNote(request.note());

        return expenseRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Page<Expense> search(LocalDate start, LocalDate end, String category, String q,
                                int page, int size, String sortBy, String sortDir) {

        Sort sort = Sort.by("date").descending();
        if (sortBy != null && !sortBy.isBlank()) {
            String property = switch (sortBy) {
                case "date" -> "date";
                case "amount" -> "amount";
                default -> throw new IllegalArgumentException("Unsupported sortBy: " + sortBy);
            };
            Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(dir, property);
        }

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200), sort);

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateBetween(start, end))
                .and(ExpenseSpecifications.categoryEquals(category))
                .and(ExpenseSpecifications.noteContains(q));

        return expenseRepository.findAll(spec, pageable);
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new NotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return expenseRepository.sumAmountBetween(start, end);
    }

    @Transactional(readOnly = true)
    public List<ExpenseRepository.CategoryTotalRow> monthlyCategoryTotals(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return expenseRepository.sumByCategoryBetween(start, end);
    }

    @Transactional(readOnly = true)
    public List<Expense> findBetween(LocalDate start, LocalDate end) {
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateBetween(start, end));
        return expenseRepository.findAll(spec, Sort.by("date").descending());
    }
}
