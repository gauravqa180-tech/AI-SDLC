package com.ai.sdlc.expensetracker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_expense_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringExpenseRule {

    public enum Frequency {
        WEEKLY,
        MONTHLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(length = 1000)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Frequency frequency;

    /**
     * Start date for generation.
     */
    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private boolean paused;

    /**
     * Last date we generated an instance for this rule (inclusive). Null means nothing generated yet.
     */
    private LocalDate lastGeneratedDate;
}
