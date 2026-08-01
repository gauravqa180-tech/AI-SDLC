package com.aisdlc.expensetracker.expense.service;

import com.aisdlc.expensetracker.expense.api.dto.ExpenseRequest;
import com.aisdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.aisdlc.expensetracker.expense.domain.Expense;
import com.aisdlc.expensetracker.expense.repo.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

  private final ExpenseRepository repository;

  @Override
  public ExpenseResponse create(ExpenseRequest request) {
    Expense saved = repository.save(toEntity(null, request));
    return toResponse(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public ExpenseResponse getById(long id) {
    return toResponse(findOrThrow(id));
  }

  @Override
  public ExpenseResponse update(long id, ExpenseRequest request) {
    Expense existing = findOrThrow(id);

    existing.setAmount(request.getAmount());
    existing.setDate(request.getDate());
    existing.setCategory(request.getCategory());
    existing.setNote(request.getNote());

    return toResponse(repository.save(existing));
  }

  @Override
  public void delete(long id) {
    if (!repository.existsById(id)) {
      throw new EntityNotFoundException("Expense not found: " + id);
    }
    repository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ExpenseResponse> list(Pageable pageable) {
    return repository.findAll(pageable).map(this::toResponse);
  }

  private Expense findOrThrow(long id) {
    return repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + id));
  }

  private Expense toEntity(Long id, ExpenseRequest request) {
    return Expense.builder()
        .id(id)
        .amount(request.getAmount())
        .date(request.getDate())
        .category(request.getCategory())
        .note(request.getNote())
        .build();
  }

  private ExpenseResponse toResponse(Expense expense) {
    return ExpenseResponse.builder()
        .id(expense.getId())
        .amount(expense.getAmount())
        .date(expense.getDate())
        .category(expense.getCategory())
        .note(expense.getNote())
        .build();
  }
}
