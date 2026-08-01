package com.aisdlc.expensetracker.expense.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
  private Long id;
  private BigDecimal amount;
  private LocalDate date;
  private String category;
  private String note;
}
