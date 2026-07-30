package com.ai.sdlc.expensetracker.expense.api;

import com.ai.sdlc.expensetracker.expense.api.dto.*;
import com.ai.sdlc.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
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
  public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest req) {
    return expenseService.create(req);
  }

  @GetMapping
  public List<ExpenseResponse> list(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "date") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir
  ) {
    Sort sort = Sort.by("desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC, mapSort(sortBy));
    return expenseService.list(start, end, category, q, sort);
  }

  @PutMapping("/{id}")
  public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseUpdateRequest req) {
    return expenseService.update(id, req);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    expenseService.delete(id);
  }

  @GetMapping("/summary/monthly")
  public MonthlyTotalResponse monthlyTotal(@RequestParam String month) {
    return expenseService.monthlyTotal(YearMonth.parse(month));
  }

  @GetMapping("/summary/category")
  public List<CategoryTotalResponse> categoryTotals(@RequestParam String month) {
    return expenseService.categoryTotals(YearMonth.parse(month));
  }

  private String mapSort(String sortBy) {
    return switch (sortBy == null ? "date" : sortBy.toLowerCase()) {
      case "amount" -> "amount";
      case "category" -> "category";
      default -> "date";
    };
  }
}
