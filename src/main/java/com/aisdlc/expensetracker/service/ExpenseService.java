package com.aisdlc.expensetracker.service;

import com.aisdlc.expensetracker.api.dto.DeleteResponse;
import com.aisdlc.expensetracker.api.dto.ExpenseCreateRequest;
import com.aisdlc.expensetracker.api.dto.ExpenseResponse;
import com.aisdlc.expensetracker.api.dto.ExpenseUpdateRequest;
import com.aisdlc.expensetracker.api.mapper.ExpenseMapper;
import com.aisdlc.expensetracker.domain.Expense;
import com.aisdlc.expensetracker.repository.ExpenseRepository;
import com.aisdlc.expensetracker.web.error.NotFoundException;
import com.aisdlc.expensetracker.web.error.UndoTokenInvalidException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final UndoTokenService undoTokenService;

    @Value("${app.expense.delete.undo-ttl-seconds:10}")
    private long undoTtlSeconds;

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest request) {
        Expense saved = expenseRepository.save(expenseMapper.toEntity(request));
        return expenseMapper.toResponse(saved);
    }

    @Transactional
    public ExpenseResponse update(long id, ExpenseUpdateRequest request) {
        Expense expense = getActiveExpense(id);
        expenseMapper.updateEntity(request, expense);
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public DeleteResponse softDelete(long id) {
        Expense expense = getActiveExpense(id);
        expense.setDeleted(true);
        expense.setDeletedAt(OffsetDateTime.now());
        expenseRepository.save(expense);

        String token = undoTokenService.issueToken(expense.getId(), Duration.ofSeconds(undoTtlSeconds));
        return new DeleteResponse(expense.getId(), true, expense.getDeletedAt(), token);
    }

    @Transactional
    public ExpenseResponse undoDelete(String token) {
        var undoInfo = undoTokenService.consume(token)
                .orElseThrow(() -> new UndoTokenInvalidException("Undo token is invalid or expired"));

        Expense expense = expenseRepository.findById(undoInfo.expenseId())
                .orElseThrow(() -> new NotFoundException("Expense not found"));

        expense.setDeleted(false);
        expense.setDeletedAt(null);
        return expenseMapper.toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void hardDelete(long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    @Transactional
    public ExpenseResponse getById(long id) {
        return expenseMapper.toResponse(getActiveExpense(id));
    }

    @Transactional
    public List<ExpenseResponse> list(LocalDate startDate,
                                     LocalDate endDate,
                                     String noteContains,
                                     String category,
                                     BigDecimal minAmount,
                                     BigDecimal maxAmount,
                                     String sortBy,
                                     String sortDir) {

        Specification<Expense> spec = Specification.where(ExpenseSpecifications.notDeleted())
                .and(ExpenseSpecifications.dateBetween(startDate, endDate))
                .and(ExpenseSpecifications.noteContains(noteContains))
                .and(ExpenseSpecifications.categoryEquals(category))
                .and(ExpenseSpecifications.amountBetween(minAmount, maxAmount));

        Sort sort = ExpenseSpecifications.sort(sortBy, sortDir);

        return expenseRepository.findAll(spec, sort)
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    private Expense getActiveExpense(long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found"));
        if (expense.isDeleted()) {
            throw new NotFoundException("Expense not found");
        }
        return expense;
    }
}
