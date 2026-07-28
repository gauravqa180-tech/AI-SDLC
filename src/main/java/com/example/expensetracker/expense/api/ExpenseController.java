package com.example.expensetracker.expense.api;

import com.example.expensetracker.expense.api.dto.ExpenseRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.api.dto.MonthlyTotalResponse;
import com.example.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

  private final ExpenseService expenseService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExpenseResponse create(@RequestBody @Valid ExpenseRequest req) {
    return expenseService.create(req);
  }

  /**
   * List with optional filter/sort/search.
   *
   * Query params:
   * - start: yyyy-MM-dd
   * - end: yyyy-MM-dd
   * - category: string
   * - q: substring match on note (case-insensitive)
   * - sort: date_desc|date_asc|amount_desc|amount_asc
   */
  @GetMapping
  public List<ExpenseResponse> list(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
      @RequestParam(required = false) String category,
      @RequestParam(required = false, name = "q") String q,
      @RequestParam(required = false) String sort
  ) {
    return expenseService.list(start, end, category, q, sort);
  }

  @PutMapping("/{id}")
  public ExpenseResponse update(@PathVariable long id, @RequestBody @Valid ExpenseRequest req) {
    return expenseService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    expenseService.delete(id);
  }

  @PostMapping("/{id}/undo")
  public ExpenseResponse undoDelete(@PathVariable long id) {
    return expenseService.undoDelete(id);
  }

  /**
   * Monthly total.
   * month format: YYYY-MM
   */
  @GetMapping("/monthly-total")
  public MonthlyTotalResponse monthlyTotal(@RequestParam String month) {
    YearMonth ym = YearMonth.parse(month);
    BigDecimal total = expenseService.monthlyTotal(ym);
    return new MonthlyTotalResponse(month, total);
  }
}
