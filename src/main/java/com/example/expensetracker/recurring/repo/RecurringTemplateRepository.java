package com.example.expensetracker.recurring.repo;

import com.example.expensetracker.recurring.domain.RecurringTemplate;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringTemplateRepository extends JpaRepository<RecurringTemplate, Long> {
  List<RecurringTemplate> findByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}
