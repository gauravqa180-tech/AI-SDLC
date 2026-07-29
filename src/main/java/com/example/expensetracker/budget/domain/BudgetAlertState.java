package com.example.expensetracker.budget.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.YearMonth;

@Entity
@Table(name = "budget_alert_states", uniqueConstraints = {
        @UniqueConstraint(name = "uk_alert_month_category", columnNames = {"budget_month", "category"})
})
public class BudgetAlertState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "budget_month", nullable = false, length = 7)
    private String budgetMonth; // YYYY-MM

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "notified_80", nullable = false)
    private boolean notified80;

    @Column(name = "notified_100", nullable = false)
    private boolean notified100;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public YearMonth getMonth() {
        return YearMonth.parse(budgetMonth);
    }

    public void setMonth(YearMonth month) {
        this.budgetMonth = month.toString();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isNotified80() {
        return notified80;
    }

    public void setNotified80(boolean notified80) {
        this.notified80 = notified80;
    }

    public boolean isNotified100() {
        return notified100;
    }

    public void setNotified100(boolean notified100) {
        this.notified100 = notified100;
    }
}
