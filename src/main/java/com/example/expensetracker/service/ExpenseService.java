package com.example.expensetracker.service;

import com.example.expensetracker.api.dto.ExpenseRequest;
import com.example.expensetracker.api.dto.ExpenseResponse;
import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.repo.ExpenseRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listAll() {
        return expenseRepository.findAll()
                .stream()
                .map(ExpenseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(Long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        return ExpenseMapper.toResponse(e);
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        Expense e = new Expense();
        ExpenseMapper.apply(request, e);
        Expense saved = expenseRepository.save(e);
        return ExpenseMapper.toResponse(saved);
    }

    /**
     * Update with optimistic locking using the provided version.
     */
    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        if (request.version() == null) {
            throw new ConflictException("Missing version for update. Re-fetch the expense and retry.");
        }

        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

        // Manual version check for cleaner error message before JPA flush
        Long currentVersion = e.getVersion();
        if (currentVersion != null && !currentVersion.equals(request.version())) {
            throw new ConflictException("Expense was modified by another session. Please refresh and retry.");
        }

        ExpenseMapper.apply(request, e);

        try {
            Expense saved = expenseRepository.saveAndFlush(e);
            return ExpenseMapper.toResponse(saved);
        } catch (OptimisticLockingFailureException | OptimisticLockException ex) {
            throw new ConflictException("Expense was modified by another session. Please refresh and retry.");
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new NotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MonthlyTotal monthlyTotal(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        return new MonthlyTotal(month, expenseRepository.findAll().stream()
                .filter(e -> !e.getExpenseDate().isBefore(from) && !e.getExpenseDate().isAfter(to))
                .map(Expense::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
    }

    public record MonthlyTotal(YearMonth month, java.math.BigDecimal total) {
    }
}
