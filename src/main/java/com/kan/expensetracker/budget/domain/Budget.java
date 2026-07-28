package com.kan.expensetracker.budget.domain;

import com.kan.expensetracker.category.domain.Category;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

@Entity
@Table(name = "budgets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_budget_month_category", columnNames = {"budget_month", "category_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stored as YYYY-MM
    @Column(name = "budget_month", nullable = false, length = 7)
    private String budgetMonth;

    // null => overall budget
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_budgets_category"))
    private Category category;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "threshold_warn", nullable = false, precision = 5, scale = 2)
    private BigDecimal thresholdWarn;

    @Column(name = "threshold_exceed", nullable = false, precision = 5, scale = 2)
    private BigDecimal thresholdExceed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static String toMonthString(YearMonth ym) {
        return ym.toString();
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
