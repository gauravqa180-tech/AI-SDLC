package com.example.expensetracker.service;

import com.example.expensetracker.api.dto.CreateExpenseRequest;
import com.example.expensetracker.api.dto.UpdateExpenseRequest;
import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.domain.ExpenseCategory;
import com.example.expensetracker.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
    public Expense create(CreateExpenseRequest req) {
        Expense expense = Expense.builder()
                .amount(req.amount())
                .date(req.date())
                .category(req.category())
                .note(req.note())
                .build();
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense update(Long id, UpdateExpenseRequest req) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
        expense.setAmount(req.amount());
        expense.setDate(req.date());
        expense.setCategory(req.category());
        expense.setNote(req.note());
        return expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public List<Expense> search(LocalDate startDate,
                               LocalDate endDate,
                               ExpenseCategory category,
                               String q,
                               Sort sort) {
        return expenseRepository.search(startDate, endDate, category, q, sort);
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new EntityNotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(YearMonth month) {
        MonthRange r = MonthRange.of(month);
        return expenseRepository.sumAmountBetween(r.start(), r.end());
    }

    @Transactional(readOnly = true)
    public List<Object[]> monthlyTotalsByCategory(YearMonth month) {
        MonthRange r = MonthRange.of(month);
        return expenseRepository.sumByCategoryBetween(r.start(), r.end());
    }
}
