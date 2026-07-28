package com.example.expensetracker.recurring.repo;

import com.example.expensetracker.recurring.domain.RecurringOccurrence;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringOccurrenceRepository extends JpaRepository<RecurringOccurrence, Long> {
  Optional<RecurringOccurrence> findByTemplateIdAndOccurrenceDate(Long templateId, LocalDate occurrenceDate);
}
