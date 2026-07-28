package com.example.expensetracker.expense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_date", columnList = "expense_date"),
        @Index(name = "idx_expense_category", columnList = "category")
})
@Getter
@Setter
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate date;

    @Column(name = "category", nullable = false, length = 64)
    private String category;

    @Column(name = "note", length = 512)
    private String note;
}
