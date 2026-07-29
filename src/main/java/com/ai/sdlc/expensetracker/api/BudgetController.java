package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.api.dto.BudgetStatusResponse;
import com.ai.sdlc.expensetracker.api.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * KAN-142: create/update budget
     */
    @PostMapping
    public BudgetStatusResponse upsert(@Valid @RequestBody BudgetUpsertRequest req) {
        return budgetService.upsert(req);
    }

    /**
     * KAN-142: budget status (includes warn/exceeded)
     */
    @GetMapping("/status")
    public BudgetStatusResponse status(@RequestParam String period, @RequestParam(required = false) String category) {
        return budgetService.status(period, category);
    }
}
