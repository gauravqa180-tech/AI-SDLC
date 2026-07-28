package com.example.expensetracker.recurring.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "recurring_occurrences", uniqueConstraints = {
    @UniqueConstraint(name = "uk_recurring_occurrence", columnNames = {"template_id", "occurrence_date"})
})
public class RecurringOccurrence {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "template_id", nullable = false)
  private Long templateId;

  @Column(name = "occurrence_date", nullable = false)
  private LocalDate occurrenceDate;

  @Column(name = "expense_id", nullable = false)
  private Long expenseId;
}
