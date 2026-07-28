package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.api.dto.BudgetResponse;
import com.ai.sdlc.expensetracker.api.dto.BudgetUpsertRequest;
import com.ai.sdlc.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * User Story 5: Set/update budgets.
     */
    @PostMapping
    public BudgetResponse upsert(@Valid @RequestBody BudgetUpsertRequest req) {
        return budgetService.upsert(req);
    }

    @GetMapping
    public List<BudgetResponse> listByMonth(@RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$") String month) {
        return budgetService.listByMonth(month);
    }
}
