package com.ai.sdlc.expensetracker.expense.service;

import com.ai.sdlc.expensetracker.common.api.NotFoundException;
import com.ai.sdlc.expensetracker.expense.api.dto.*;
import com.ai.sdlc.expensetracker.expense.domain.Expense;
import com.ai.sdlc.expensetracker.expense.repo.ExpenseRepository;
import java.math.BigDecimal;
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

  @Transactional
  public ExpenseResponse create(ExpenseCreateRequest req) {
    Expense saved = expenseRepository.save(Expense.builder()
        .amount(req.amount())
        .date(req.date())
        .category(req.category())
        .note(req.note())
        .build());

    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<ExpenseResponse> list(LocalDate start, LocalDate end, String category, String q, Sort sort) {
    return expenseRepository.search(start, end, blankToNull(category), blankToNull(q), sort)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public ExpenseResponse update(long id, ExpenseUpdateRequest req) {
    Expense e = expenseRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

    e.setAmount(req.amount());
    e.setDate(req.date());
    e.setCategory(req.category());
    e.setNote(req.note());

    return toResponse(expenseRepository.save(e));
  }

  @Transactional
  public void delete(long id) {
    if (!expenseRepository.existsById(id)) {
      throw new NotFoundException("Expense not found: " + id);
    }
    expenseRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public MonthlyTotalResponse monthlyTotal(YearMonth month) {
    LocalDate start = month.atDay(1);
    LocalDate end = month.atEndOfMonth();
    BigDecimal total = expenseRepository.sumAmountBetween(start, end);
    return new MonthlyTotalResponse(month, total);
  }

  @Transactional(readOnly = true)
  public List<CategoryTotalResponse> categoryTotals(YearMonth month) {
    LocalDate start = month.atDay(1);
    LocalDate end = month.atEndOfMonth();
    return expenseRepository.totalsByCategory(start, end).stream()
        .map(p -> new CategoryTotalResponse(p.getCategory(), p.getTotal()))
        .toList();
  }

  private ExpenseResponse toResponse(Expense e) {
    return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
  }

  private static String blankToNull(String s) {
    return (s == null || s.isBlank()) ? null : s;
  }
}
