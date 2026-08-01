package com.aisdlc.expensetracker.expense.api;

import com.aisdlc.expensetracker.expense.api.dto.ExpenseRequest;
import com.aisdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.aisdlc.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

  private final ExpenseService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExpenseResponse create(@Valid @RequestBody ExpenseRequest request) {
    return service.create(request);
  }

  @GetMapping("/{id}")
  public ExpenseResponse get(@PathVariable long id) {
    return service.getById(id);
  }

  @PutMapping("/{id}")
  public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    service.delete(id);
  }

  @GetMapping
  public Page<ExpenseResponse> list(@PageableDefault(size = 20) Pageable pageable) {
    return service.list(pageable);
  }
}
