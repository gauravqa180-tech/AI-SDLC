package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.api.dto.ExpenseRequest;
import com.ai.sdlc.expensetracker.domain.Expense;
import com.ai.sdlc.expensetracker.repository.ExpenseRepository;
import com.ai.sdlc.expensetracker.service.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public Expense create(ExpenseRequest request) {
        Expense expense = new Expense(request.amount(), request.date(), request.category(), request.note());
        return expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public List<Expense> list() {
        return expenseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Expense get(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
    }

    @Transactional
    public Expense update(Long id, ExpenseRequest request) {
        Expense existing = get(id);
        existing.setAmount(request.amount());
        existing.setDate(request.date());
        existing.setCategory(request.category());
        existing.setNote(request.note());
        return existing;
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new NotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal monthlyTotal(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return expenseRepository.sumAmountBetween(start, end);
    }
}
