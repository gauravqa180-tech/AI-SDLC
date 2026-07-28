package com.ai.sdlc.expensetracker.budgets.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "budget_alert_state",
        uniqueConstraints = @UniqueConstraint(name = "uk_alert_month_category", columnNames = {"budget_month", "category"}),
        indexes = @Index(name = "idx_alert_month", columnList = "budget_month")
)
public class BudgetAlertState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "budget_month", nullable = false, length = 7)
    private String month; // YYYY-MM

    @Column(name = "category", length = 100)
    private String category; // null => overall

    @Column(name = "warn_sent", nullable = false)
    private boolean warnSent;

    @Column(name = "exceed_sent", nullable = false)
    private boolean exceedSent;

    protected BudgetAlertState() {
    }

    public BudgetAlertState(String month, String category) {
        this.month = month;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getMonth() {
        return month;
    }

    public String getCategory() {
        return category;
    }

    public boolean isWarnSent() {
        return warnSent;
    }

    public void setWarnSent(boolean warnSent) {
        this.warnSent = warnSent;
    }

    public boolean isExceedSent() {
        return exceedSent;
    }

    public void setExceedSent(boolean exceedSent) {
        this.exceedSent = exceedSent;
    }
}
