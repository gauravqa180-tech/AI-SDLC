package com.ai.sdlc.expensetracker.api;

import com.ai.sdlc.expensetracker.api.dto.RecurringRuleRequest;
import com.ai.sdlc.expensetracker.api.dto.RecurringRuleResponse;
import com.ai.sdlc.expensetracker.service.RecurringExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
public class RecurringExpenseController {

    private final RecurringExpenseService recurringExpenseService;

    @PostMapping
    public RecurringRuleResponse create(@Valid @RequestBody RecurringRuleRequest req) {
        return recurringExpenseService.create(req);
    }

    @PutMapping("/{id}")
    public RecurringRuleResponse update(@PathVariable long id, @Valid @RequestBody RecurringRuleRequest req) {
        return recurringExpenseService.update(id, req);
    }

    @GetMapping
    public List<RecurringRuleResponse> list() {
        return recurringExpenseService.list();
    }

    /**
     * KAN-142: manual generation endpoint (instead of background job in v1)
     */
    @PostMapping("/generate")
    public int generate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate upToDate) {
        return recurringExpenseService.generateUpTo(upToDate);
    }
}
