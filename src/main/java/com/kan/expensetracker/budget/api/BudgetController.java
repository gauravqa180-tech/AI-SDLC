package com.kan.expensetracker.budget.api;

import com.kan.expensetracker.budget.api.dto.BudgetRequest;
import com.kan.expensetracker.budget.api.dto.BudgetResponse;
import com.kan.expensetracker.budget.api.dto.BudgetStatusResponse;
import com.kan.expensetracker.budget.service.BudgetMapper;
import com.kan.expensetracker.budget.service.BudgetService;
import com.kan.expensetracker.budget.service.BudgetStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetStatusService budgetStatusService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse upsert(@Valid @RequestBody BudgetRequest request) {
        return BudgetMapper.toResponse(budgetService.upsert(request));
    }

    @GetMapping
    public List<BudgetResponse> listForMonth(@RequestParam int year, @RequestParam int month) {
        return budgetService.listForMonth(year, month).stream().map(BudgetMapper::toResponse).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        budgetService.delete(id);
    }

    // US5: status + alerts
    @GetMapping("/status")
    public BudgetStatusResponse status(@RequestParam int year, @RequestParam int month) {
        // We record alerts whenever status is requested (simple in-app alert model)
        return budgetStatusService.calculateStatus(year, month, true);
    }
}
