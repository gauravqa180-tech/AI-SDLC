package com.ai.sdlc.expensetracker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "expenses",
        indexes = {
                @Index(name = "idx_expenses_date", columnList = "expense_date"),
                @Index(name = "idx_expenses_category", columnList = "category"),
                @Index(name = "idx_expenses_date_category", columnList = "expense_date, category")
        }
)
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate date;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "note", length = 500)
    private String note;
}
