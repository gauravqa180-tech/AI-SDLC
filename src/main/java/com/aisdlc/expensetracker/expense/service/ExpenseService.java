package com.aisdlc.expensetracker.expense.service;

import com.aisdlc.expensetracker.expense.api.dto.ExpenseRequest;
import com.aisdlc.expensetracker.expense.api.dto.ExpenseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpenseService {

  ExpenseResponse create(ExpenseRequest request);

  ExpenseResponse getById(long id);

  ExpenseResponse update(long id, ExpenseRequest request);

  void delete(long id);

  Page<ExpenseResponse> list(Pageable pageable);
}
