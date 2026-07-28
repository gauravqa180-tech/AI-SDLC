package com.kan.expensetracker.budget.domain;

import com.kan.expensetracker.category.domain.Category;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "budget_alert_state", uniqueConstraints = {
        @UniqueConstraint(name = "uk_alert_month_category_threshold", columnNames = {"budget_month", "category_id", "threshold_type"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetAlertState {

    public enum ThresholdType {
        WARN_80,
        EXCEED_100
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "budget_month", nullable = false, length = 7)
    private String budgetMonth;

    // null => overall budget
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_budget_alert_state_category"))
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "threshold_type", nullable = false, length = 20)
    private ThresholdType thresholdType;

    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;
}
