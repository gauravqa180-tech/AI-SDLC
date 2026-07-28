package com.example.expensetracker.expense.service;

import com.example.expensetracker.expense.api.dto.ExpenseRequest;
import com.example.expensetracker.expense.api.dto.ExpenseResponse;
import com.example.expensetracker.expense.domain.DeletedExpense;
import com.example.expensetracker.expense.domain.Expense;

final class ExpenseMapper {

  private ExpenseMapper() {}

  static ExpenseResponse toResponse(Expense e) {
    return new ExpenseResponse(e.getId(), e.getAmount(), e.getDate(), e.getCategory(), e.getNote());
  }

  static void applyRequest(Expense target, ExpenseRequest req) {
    target.setAmount(req.amount());
    target.setDate(req.date());
    target.setCategory(req.category());
    target.setNote(req.note());
  }

  static DeletedExpense toDeleted(Expense e) {
    DeletedExpense d = new DeletedExpense();
    d.setId(e.getId());
    d.setAmount(e.getAmount());
    d.setDate(e.getDate());
    d.setCategory(e.getCategory());
    d.setNote(e.getNote());
    return d;
  }

  static Expense fromDeleted(DeletedExpense d) {
    Expense e = new Expense();
    e.setId(d.getId());
    e.setAmount(d.getAmount());
    e.setDate(d.getDate());
    e.setCategory(d.getCategory());
    e.setNote(d.getNote());
    return e;
  }
}
