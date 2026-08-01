package com.aisdlc.expensetracker.expense.service;

import com.aisdlc.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.aisdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.aisdlc.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.aisdlc.expensetracker.expense.api.dto.MonthlyTotalResponse;
import com.aisdlc.expensetracker.expense.domain.Expense;
import com.aisdlc.expensetracker.expense.repository.ExpenseRepository;
import com.aisdlc.expensetracker.shared.error.NotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final Clock clock = Clock.systemUTC();

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request) {
        Expense expense = new Expense();
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setCategory(request.category().trim());
        expense.setNote(request.note());
        Instant now = Instant.now(clock);
        expense.setCreatedAt(now);
        expense.setUpdatedAt(now);

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list() {
        return expenseRepository.findAll(Sort.by(Sort.Direction.DESC, "expenseDate").and(Sort.by("id")))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(long id) {
        if (!expenseRepository.existsById(id)) {
            throw new NotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    // User story #1: edit expense
    @Transactional
    public ExpenseResponse update(long id, ExpenseUpdateRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setCategory(request.category().trim());
        expense.setNote(request.note());
        expense.setUpdatedAt(Instant.now(clock));

        return toResponse(expense);
    }

    @Transactional(readOnly = true)
    public MonthlyTotalResponse monthlyTotal(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        BigDecimal total = expenseRepository.sumAmountBetween(start, end);
        return new MonthlyTotalResponse(year, month, total);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getAmount(), e.getExpenseDate(), e.getCategory(), e.getNote());
    }
}
