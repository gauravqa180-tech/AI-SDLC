package com.aisdlc.expensetracker.expense.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRequest {

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be a positive number")
  private BigDecimal amount;

  @NotNull(message = "Date is required")
  private LocalDate date;

  @NotBlank(message = "Category is required")
  @Size(max = 100, message = "Category must be at most 100 characters")
  private String category;

  @Size(max = 500, message = "Note must be at most 500 characters")
  private String note;
}
