package com.ai.sdlc.expensetracker.expense.service;

import com.ai.sdlc.expensetracker.common.error.ResourceNotFoundException;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.ai.sdlc.expensetracker.expense.api.dto.MonthlyTotalResponse;
import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request) {
        Expense saved = repository.save(ExpenseMapper.toEntity(request));
        return ExpenseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listAll() {
        return repository.findAll()
                .stream()
                .map(ExpenseMapper::toResponse)
                .toList();
    }

    @Transactional
    public ExpenseResponse update(long id, ExpenseUpdateRequest request) {
        Expense e = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        ExpenseMapper.apply(request, e);
        return ExpenseMapper.toResponse(repository.save(e));
    }

    @Transactional
    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MonthlyTotalResponse monthlyTotal(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        BigDecimal total = repository.sumAmountBetween(start, end);
        return new MonthlyTotalResponse(year, month, total);
    }
}
