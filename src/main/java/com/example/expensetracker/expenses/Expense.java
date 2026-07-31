package com.example.expensetracker.expenses;

import com.example.expensetracker.categories.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_date", columnList = "expenseDate"),
        @Index(name = "idx_expense_category", columnList = "category_id"),
        @Index(name = "idx_expense_deleted", columnList = "deletedAt")
})
@Getter
@Setter
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(length = 500)
    private String note;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    private Instant deletedAt;

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
