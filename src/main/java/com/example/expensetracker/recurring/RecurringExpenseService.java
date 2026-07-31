package com.example.expensetracker.recurring;

import com.example.expensetracker.categories.Category;
import com.example.expensetracker.categories.CategoryService;
import com.example.expensetracker.expenses.Expense;
import com.example.expensetracker.expenses.ExpenseRepository;
import com.example.expensetracker.recurring.dto.RecurringExpenseDto;
import com.example.expensetracker.recurring.dto.RecurringExpenseUpsertRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurringExpenseService {

    private final RecurringExpenseRepository recurringRepo;
    private final RecurringExpenseOccurrenceRepository occurrenceRepo;
    private final CategoryService categoryService;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public List<RecurringExpenseDto> list() {
        return recurringRepo.findAll().stream().map(RecurringExpenseService::toDto).toList();
    }

    @Transactional
    public RecurringExpenseDto create(RecurringExpenseUpsertRequest req) {
        Category category = categoryService.requireCategory(req.categoryId());
        RecurringExpense r = new RecurringExpense();
        r.setAmount(req.amount());
        r.setCategory(category);
        r.setNote(req.note());
        r.setCadence(req.cadence());
        r.setStartDate(req.startDate());
        r.setEnabled(Boolean.TRUE.equals(req.enabled()));
        return toDto(recurringRepo.save(r));
    }

    @Transactional
    public RecurringExpenseDto update(long id, RecurringExpenseUpsertRequest req) {
        RecurringExpense r = recurringRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recurring expense not found: " + id));
        Category category = categoryService.requireCategory(req.categoryId());
        r.setAmount(req.amount());
        r.setCategory(category);
        r.setNote(req.note());
        r.setCadence(req.cadence());
        r.setStartDate(req.startDate());
        r.setEnabled(Boolean.TRUE.equals(req.enabled()));
        return toDto(recurringRepo.save(r));
    }

    @Transactional
    public void delete(long id) {
        recurringRepo.deleteById(id);
    }

    @Transactional
    public GenerationResult generateUpTo(LocalDate upTo) {
        List<GeneratedOccurrence> generated = new ArrayList<>();

        for (RecurringExpense r : recurringRepo.findAll()) {
            if (!r.isEnabled()) continue;
            LocalDate next = r.getStartDate();
            while (!next.isAfter(upTo)) {
                if (occurrenceRepo.findByRecurringExpenseIdAndOccurrenceDate(r.getId(), next).isEmpty()) {
                    Expense e = new Expense();
                    e.setAmount(r.getAmount());
                    e.setExpenseDate(next);
                    e.setCategory(r.getCategory());
                    e.setNote(r.getNote());
                    Expense saved = expenseRepository.save(e);

                    RecurringExpenseOccurrence occ = new RecurringExpenseOccurrence();
                    occ.setRecurringExpense(r);
                    occ.setOccurrenceDate(next);
                    occ.setExpense(saved);
                    occurrenceRepo.save(occ);

                    generated.add(new GeneratedOccurrence(r.getId(), next, saved.getId()));
                }
                next = increment(next, r.getCadence());
            }
        }

        return new GenerationResult(generated.size(), generated);
    }

    private LocalDate increment(LocalDate date, RecurringCadence cadence) {
        return switch (cadence) {
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
        };
    }

    static RecurringExpenseDto toDto(RecurringExpense r) {
        return new RecurringExpenseDto(
                r.getId(),
                r.getAmount(),
                r.getCategory().getId(),
                r.getCategory().getName(),
                r.getNote(),
                r.getCadence(),
                r.getStartDate(),
                r.isEnabled()
        );
    }

    public record GenerationResult(int generatedCount, List<GeneratedOccurrence> occurrences) {}
    public record GeneratedOccurrence(Long recurringId, LocalDate date, Long expenseId) {}
}
