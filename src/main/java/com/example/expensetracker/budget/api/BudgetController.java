package com.example.expensetracker.budget.api;

import com.example.expensetracker.budget.api.dto.BudgetProgressResponse;
import com.example.expensetracker.budget.api.dto.BudgetResponse;
import com.example.expensetracker.budget.api.dto.BudgetUpsertRequest;
import com.example.expensetracker.budget.service.BudgetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService service;

    // User Story 5: create/update budget
    @PostMapping
    public ResponseEntity<BudgetResponse> upsert(@Valid @RequestBody BudgetUpsertRequest request) {
        return ResponseEntity.ok(service.upsert(request));
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> list(@RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$") String month) {
        return ResponseEntity.ok(service.list(month));
    }

    // progress + in-app alert indicator
    @GetMapping("/progress")
    public ResponseEntity<BudgetProgressResponse> progress(
            @RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$") String month,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(service.progress(month, category));
    }
}
