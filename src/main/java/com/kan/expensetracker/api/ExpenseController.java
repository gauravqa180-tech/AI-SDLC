package com.kan.expensetracker.api;

import com.kan.expensetracker.api.dto.ExpenseCreateRequest;
import com.kan.expensetracker.api.dto.ExpenseResponse;
import com.kan.expensetracker.api.dto.ExpenseUpdateRequest;
import com.kan.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest request) {
        return expenseService.create(request);
    }

    @GetMapping
    public List<ExpenseResponse> list() {
        return expenseService.list();
    }

    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateRequest request) {
        return expenseService.update(id, request);
    }

    /**
     * Soft-delete to allow UI "Undo".
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        expenseService.softDelete(id);
    }

    /**
     * Restore a previously deleted expense (undo delete).
     */
    @PostMapping("/{id}/restore")
    public ExpenseResponse restore(@PathVariable Long id) {
        return expenseService.restore(id);
    }
}
