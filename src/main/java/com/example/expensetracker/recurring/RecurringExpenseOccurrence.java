package com.example.expensetracker.recurring;

import com.example.expensetracker.expenses.Expense;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "recurring_occurrences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recurring_occurrence",
                columnNames = {"recurring_expense_id", "occurrence_date"}
        ))
@Getter
@Setter
public class RecurringExpenseOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_expense_id", nullable = false)
    private RecurringExpense recurringExpense;

    @Column(name = "occurrence_date", nullable = false)
    private LocalDate occurrenceDate;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;
}
