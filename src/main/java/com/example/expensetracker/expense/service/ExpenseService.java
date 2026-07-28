package com.example.expensetracker.expense.service;

import com.example.expensetracker.expense.api.dto.ExpenseRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.domain.DeletedExpense;
import com.example.expensetracker.expense.domain.Expense;
import com.example.expensetracker.expense.repo.DeletedExpenseRepository;
import com.example.expensetracker.expense.repo.ExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

  private final ExpenseRepository expenseRepository;
  private final DeletedExpenseRepository deletedExpenseRepository;

  @Value("${app.deleteUndoWindowSeconds:10}")
  private long undoWindowSeconds;

  @Transactional
  public ExpenseResponse create(ExpenseRequest req) {
    Expense e = new Expense();
    ExpenseMapper.applyRequest(e, req);
    return ExpenseMapper.toResponse(expenseRepository.save(e));
  }

  @Transactional(readOnly = true)
  public List<ExpenseResponse> list(LocalDate start, LocalDate end, String category, String q, String sort) {
    Specification<Expense> spec = Specification.where(ExpenseSpecifications.dateGte(start))
        .and(ExpenseSpecifications.dateLte(end))
        .and(ExpenseSpecifications.categoryEq(category))
        .and(ExpenseSpecifications.noteContains(q));

    return expenseRepository.findAll(spec, toSort(sort)).stream().map(ExpenseMapper::toResponse).toList();
  }

  @Transactional
  public ExpenseResponse update(long id, ExpenseRequest req) {
    Expense e = expenseRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Expense not found: " + id));
    ExpenseMapper.applyRequest(e, req);
    return ExpenseMapper.toResponse(expenseRepository.save(e));
  }

  /**
   * Delete with undo support by moving row to deleted_expenses.
   */
  @Transactional
  public void delete(long id) {
    Expense e = expenseRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Expense not found: " + id));
    deletedExpenseRepository.save(ExpenseMapper.toDeleted(e));
    expenseRepository.delete(e);
  }

  /**
   * Undo delete only within configured undo window.
   */
  @Transactional
  public ExpenseResponse undoDelete(long id) {
    DeletedExpense d = deletedExpenseRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Deleted expense not found or undo window expired: " + id));

    OffsetDateTime cutoff = OffsetDateTime.now().minusSeconds(undoWindowSeconds);
    if (d.getDeletedAt().isBefore(cutoff)) {
      // treat as expired
      deletedExpenseRepository.deleteById(id);
      throw new NoSuchElementException("Undo window expired for expense: " + id);
    }

    Expense restored = ExpenseMapper.fromDeleted(d);
    Expense saved = expenseRepository.save(restored);
    deletedExpenseRepository.deleteById(id);
    return ExpenseMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public BigDecimal monthlyTotal(YearMonth ym) {
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();
    return expenseRepository.sumBetween(start, end);
  }

  @Transactional
  public int purgeExpiredDeleted() {
    OffsetDateTime cutoff = OffsetDateTime.now().minusSeconds(undoWindowSeconds);
    List<DeletedExpense> expired = deletedExpenseRepository.findByDeletedAtBefore(cutoff);
    deletedExpenseRepository.deleteAllInBatch(expired);
    return expired.size();
  }

  private Sort toSort(String sort) {
    // sort values: date_desc (default), date_asc, amount_desc, amount_asc
    String s = (sort == null || sort.isBlank()) ? "date_desc" : sort;

    return switch (s) {
      case "date_asc" -> Sort.by(Sort.Direction.ASC, "date");
      case "amount_desc" -> Sort.by(Sort.Direction.DESC, "amount");
      case "amount_asc" -> Sort.by(Sort.Direction.ASC, "amount");
      case "date_desc" -> Sort.by(Sort.Direction.DESC, "date");
      default -> Sort.by(Sort.Direction.DESC, "date");
    };
  }
}
