package com.aisdlc.expensetracker.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_date", columnList = "expense_date"),
        @Index(name = "idx_expense_category", columnList = "category")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ExpenseCategory category;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (category == null) {
            category = ExpenseCategory.OTHER;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
