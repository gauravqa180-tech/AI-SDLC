package com.example.expensetracker.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import lombok.*;

@Entity
@Table(
    name = "budgets",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_budget_month_category", columnNames = {"budget_month", "category"})
    },
    indexes = {
        @Index(name = "idx_budgets_month", columnList = "budget_month"),
        @Index(name = "idx_budgets_category", columnList = "category")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Stored as YYYY-MM (e.g. 2026-07). */
  @Column(name = "budget_month", nullable = false, length = 7)
  private String month;

  /** Nullable means overall monthly budget. */
  @Column(name = "category", length = 64)
  private String category;

  @Column(name = "amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    this.updatedAt = Instant.now();
  }

  public YearMonth monthAsYearMonth() {
    return YearMonth.parse(month);
  }
}
