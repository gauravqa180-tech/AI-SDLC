package com.ai.sdlc.expensetracker.service;

import com.ai.sdlc.expensetracker.api.dto.ExpenseRequest;
import com.ai.sdlc.expensetracker.domain.Expense;
import com.ai.sdlc.expensetracker.repository.ExpenseRepository;
import com.ai.sdlc.expensetracker.repository.ExpenseSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Value("${app.delete.undo-window-seconds:10}")
    private long undoWindowSeconds;

    @Transactional
    public Expense create(ExpenseRequest request) {
        Expense e = new Expense();
        e.setAmount(request.amount());
        e.setExpenseDate(request.date());
        e.setCategory(request.category().trim());
        e.setNote(request.note());
        return expenseRepository.save(e);
    }

    @Transactional
    public Expense update(long id, ExpenseRequest request) {
        Expense e = expenseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        e.setAmount(request.amount());
        e.setExpenseDate(request.date());
        e.setCategory(request.category().trim());
        e.setNote(request.note());
        return expenseRepository.save(e);
    }

    @Transactional
    public Expense get(long id) {
        return expenseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
    }

    @Transactional
    public void softDelete(long id) {
        Expense e = expenseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        e.setDeletedAt(Instant.now());
        expenseRepository.save(e);
    }

    @Transactional
    public Expense undoDelete(long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        if (e.getDeletedAt() == null) {
            return e;
        }
        Instant deletedAt = e.getDeletedAt();
        Instant latestAllowed = Instant.now().minusSeconds(undoWindowSeconds);
        if (deletedAt.isBefore(latestAllowed)) {
            throw new UndoNotAllowedException("Undo window expired for expense: " + id);
        }
        e.setDeletedAt(null);
        return expenseRepository.save(e);
    }

    @Transactional
    public Page<Expense> list(LocalDate from, LocalDate to, String category, BigDecimal minAmount, BigDecimal maxAmount, String q,
                             List<String> sort, int page, int size) {

        Sort defaultSort = Sort.by(Sort.Order.desc("expenseDate"), Sort.Order.desc("id"));
        Sort parsedSort = SortParser.parse(sort, defaultSort);

        Pageable pageable = PageRequest.of(Math.max(page, 0), clampSize(size), parsedSort);
        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted())
                .and(ExpenseSpecifications.dateFrom(from))
                .and(ExpenseSpecifications.dateTo(to))
                .and(ExpenseSpecifications.categoryEquals(category))
                .and(ExpenseSpecifications.amountMin(minAmount))
                .and(ExpenseSpecifications.amountMax(maxAmount))
                .and(ExpenseSpecifications.noteContains(q));

        return expenseRepository.findAll(spec, pageable);
    }

    private int clampSize(int size) {
        if (size <= 0) return 20;
        return Math.min(size, 200);
    }
}
