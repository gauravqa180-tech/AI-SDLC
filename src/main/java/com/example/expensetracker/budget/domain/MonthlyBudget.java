package com.example.expensetracker.budget.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "monthly_budgets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_monthly_budgets_month", columnNames = {"budget_month"})
})
public class MonthlyBudget {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** YYYY-MM */
  @Column(name = "budget_month", nullable = false, length = 7)
  private String month;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "warned_80", nullable = false)
  private boolean warned80;

  @Column(name = "warned_100", nullable = false)
  private boolean warned100;
}
