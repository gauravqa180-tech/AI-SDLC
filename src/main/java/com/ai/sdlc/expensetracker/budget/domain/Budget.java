package com.ai.sdlc.expensetracker.budget.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import lombok.*;

@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_budget_month_category", columnNames = {"budget_month", "category"})
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

  @Column(name = "budget_month", nullable = false, length = 7)
  @Convert(converter = YearMonthAttributeConverter.class)
  private YearMonth month;

  @Column(name = "category", length = 100)
  private String category; // null => overall monthly budget

  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private java.math.BigDecimal amount;
}
