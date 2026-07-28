package com.example.expensetracker.expense.service;

import com.example.expensetracker.expense.api.dto.ExpenseCreateRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.api.dto.ExpenseUpdateRequest;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req) {
        Expense expense = Expense.builder()
                .amount(req.amount())
                .date(req.date())
                .category(req.category())
                .note(req.note())
                .build();

        Expense saved = expenseRepository.save(expense);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list() {
        return expenseRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        return expenseRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest req) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));

        // Manual optimistic-lock check (in addition to @Version) so we can return a clear 409.
        if (existing.getVersion() == null || !existing.getVersion().equals(req.version())) {
            throw new ExpenseConflictException(id, existing.getVersion(), req.version());
        }

        existing.setAmount(req.amount());
        existing.setDate(req.date());
        existing.setCategory(req.category());
        existing.setNote(req.note());

        try {
            Expense saved = expenseRepository.save(existing);
            return toResponse(saved);
        } catch (OptimisticLockingFailureException | OptimisticLockException e) {
            // In case JPA detects conflict at flush time
            throw new ExpenseConflictException(id, existing.getVersion(), req.version());
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ExpenseNotFoundException(id);
        }
        expenseRepository.deleteById(id);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getAmount(),
                e.getDate(),
                e.getCategory(),
                e.getNote(),
                e.getVersion()
        );
    }
}
