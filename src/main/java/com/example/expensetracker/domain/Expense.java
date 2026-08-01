package com.example.expensetracker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses",
        indexes = {
                @Index(name = "idx_expense_date", columnList = "expense_date"),
                @Index(name = "idx_expense_category", columnList = "category"),
                @Index(name = "idx_expense_date_category", columnList = "expense_date,category")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate date;

    @Column(name = "category", length = 64, nullable = false)
    private String category;

    @Column(name = "note", length = 255)
    private String note;
}
