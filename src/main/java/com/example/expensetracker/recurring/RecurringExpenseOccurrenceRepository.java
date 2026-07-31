package com.example.expensetracker.recurring;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface RecurringExpenseOccurrenceRepository extends JpaRepository<RecurringExpenseOccurrence, Long> {
    Optional<RecurringExpenseOccurrence> findByRecurringExpenseIdAndOccurrenceDate(Long recurringExpenseId, LocalDate occurrenceDate);
}
