package com.ai.sdlc.expensetracker.expense.api;

import com.ai.sdlc.expensetracker.expense.api.dto.ExpenseResponse;
import com.ai.sdlc.expensetracker.expense.api.dto.UpdateExpenseRequest;
import com.ai.sdlc.expensetracker.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/expenses", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * User Story 1 (KAN-11): Edit an expense
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }
}
