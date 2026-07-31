package com.example.expensetracker.expense;

import com.example.expensetracker.category.Category;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expenses_expense_date", columnList = "expenseDate"),
        @Index(name = "idx_expenses_deleted_at", columnList = "deletedAt"),
        @Index(name = "idx_expenses_category_id", columnList = "category_id")
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

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_expenses_category"))
    private Category category;

    @Column(length = 255)
    private String note;

    /** Soft delete timestamp. If not null, record is considered deleted. */
    private OffsetDateTime deletedAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
