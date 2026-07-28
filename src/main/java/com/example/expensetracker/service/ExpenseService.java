package com.example.expensetracker.service;

import com.example.expensetracker.api.dto.*;
import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.repo.ExpenseRepository;
import jakarta.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
        .category(req.category().trim())
        .note(req.note())
        .build());
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<ExpenseResponse> list(LocalDate start, LocalDate end, String category,
                                   BigDecimal minAmount, BigDecimal maxAmount, String q,
                                   Sort sort) {

    Specification<Expense> spec = Specification.where(null);

    if (start != null && end != null) {
      spec = spec.and(ExpenseSpecifications.dateBetween(start, end));
    } else if (start != null) {
      spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), start));
    } else if (end != null) {
      spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), end));
    }

    if (category != null && !category.isBlank()) {
      spec = spec.and(ExpenseSpecifications.categoryEquals(category.trim()));
    }

    if (minAmount != null) {
      spec = spec.and(ExpenseSpecifications.amountGte(minAmount));
    }

    if (maxAmount != null) {
      spec = spec.and(ExpenseSpecifications.amountLte(maxAmount));
    }

    if (q != null && !q.isBlank()) {
      spec = spec.and(ExpenseSpecifications.noteContainsIgnoreCase(q.trim()));
    }

    return expenseRepository.findAll(spec, sort).stream().map(this::toResponse).toList();
  }

  @Transactional
  public ExpenseResponse update(long id, ExpenseUpdateRequest req) {
    Expense expense = expenseRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Expense not found: " + id));

    if (req.version() != null && !req.version().equals(expense.getVersion())) {
      throw new ConflictException("Expense has changed. Please refresh and retry.");
    }

    expense.setAmount(req.amount());
    expense.setDate(req.date());
    expense.setCategory(req.category().trim());
    expense.setNote(req.note());

    try {
      Expense saved = expenseRepository.save(expense);
      return toResponse(saved);
    } catch (OptimisticLockException | OptimisticLockingFailureException e) {
      throw new ConflictException("Expense has changed. Please refresh and retry.");
    }
  }

  @Transactional
  public void softDelete(long id) {
    if (!expenseRepository.existsById(id)) {
      throw new NotFoundException("Expense not found: " + id);
    }
    expenseRepository.deleteById(id); // triggers @SQLDelete
  }

  @Transactional
  public void restore(long id) {
    int updated = expenseRepository.restoreById(id);
    if (updated == 0) {
      throw new NotFoundException("Deleted expense not found: " + id);
    }
  }

  @Transactional(readOnly = true)
  public List<ExpenseResponse> listDeleted(Sort sort) {
    return expenseRepository.findDeleted(sort).stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public MonthlyTotalResponse monthlyTotal(String month) {
    YearMonth ym = YearMonth.parse(month);
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();
    return new MonthlyTotalResponse(month, expenseRepository.sumAmountBetween(start, end));
  }

  @Transactional(readOnly = true)
  public ReportResponse monthlyReport(String month) {
    YearMonth ym = YearMonth.parse(month);
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();

    BigDecimal total = expenseRepository.sumAmountBetween(start, end);
    List<CategoryTotalResponse> byCat = expenseRepository.sumByCategoryBetween(start, end).stream()
        .map(p -> new CategoryTotalResponse(p.getCategory(), p.getTotal()))
        .toList();

    return new ReportResponse(month, total, byCat);
  }

  private ExpenseResponse toResponse(Expense e) {
    return new ExpenseResponse(
        e.getId(),
        e.getAmount(),
        e.getDate(),
        e.getCategory(),
        e.getNote(),
        e.getCreatedAt(),
        e.getUpdatedAt(),
        e.getVersion()
    );
  }
}
