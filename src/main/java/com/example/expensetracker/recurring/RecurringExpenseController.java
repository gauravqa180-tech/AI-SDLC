package com.example.expensetracker.recurring;

import com.example.expensetracker.recurring.dto.RecurringExpenseDto;
import com.example.expensetracker.recurring.dto.RecurringExpenseUpsertRequest;
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

    private final RecurringExpenseService recurringService;

    @GetMapping
    public List<RecurringExpenseDto> list() {
        return recurringService.list();
    }

    @PostMapping
    public RecurringExpenseDto create(@Valid @RequestBody RecurringExpenseUpsertRequest req) {
        return recurringService.create(req);
    }

    @PutMapping("/{id}")
    public RecurringExpenseDto update(@PathVariable long id, @Valid @RequestBody RecurringExpenseUpsertRequest req) {
        return recurringService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        recurringService.delete(id);
    }

    @PostMapping("/generate")
    public RecurringExpenseService.GenerationResult generate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate upTo) {
        return recurringService.generateUpTo(upTo);
    }
}
