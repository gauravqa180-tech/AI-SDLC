package com.aisdlc.expensetracker.api;

import com.aisdlc.expensetracker.api.dto.*;
import com.aisdlc.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/expenses", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@Valid @RequestBody ExpenseCreateRequest request) {
        return expenseService.create(request);
    }

    @GetMapping
    public List<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String noteContains,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, defaultValue = "expenseDate") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDir
    ) {
        return expenseService.list(startDate, endDate, noteContains, category, minAmount, maxAmount, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public ExpenseResponse getById(@PathVariable long id) {
        return expenseService.getById(id);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ExpenseResponse update(@PathVariable long id, @Valid @RequestBody ExpenseUpdateRequest request) {
        return expenseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public DeleteResponse delete(@PathVariable long id,
                                 @RequestParam(defaultValue = "false") boolean hard) {
        if (hard) {
            expenseService.hardDelete(id);
            return new DeleteResponse(id, true, null, null);
        }
        return expenseService.softDelete(id);
    }

    @PostMapping(value = "/undo-delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ExpenseResponse undoDelete(@Valid @RequestBody UndoDeleteRequest request) {
        return expenseService.undoDelete(request.undoToken());
    }
}
