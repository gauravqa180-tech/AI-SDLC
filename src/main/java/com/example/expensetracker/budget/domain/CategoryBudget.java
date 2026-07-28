package com.example.expensetracker.budget.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "category_budgets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_category_budgets_month_category", columnNames = {"budget_month", "category"})
})
public class CategoryBudget {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** YYYY-MM */
  @Column(name = "budget_month", nullable = false, length = 7)
  private String month;

  @Column(nullable = false, length = 64)
  private String category;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "warned_80", nullable = false)
  private boolean warned80;

  @Column(name = "warned_100", nullable = false)
  private boolean warned100;
}
