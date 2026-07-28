package com.example.expensetracker.expense.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "deleted_expenses")
public class DeletedExpense {

  /**
   * Keep original expense id so undo restores the same id.
   */
  @Id
  private Long id;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "expense_date", nullable = false)
  private LocalDate date;

  @Column(nullable = false, length = 64)
  private String category;

  @Column(length = 255)
  private String note;

  @Column(name = "deleted_at", nullable = false)
  private OffsetDateTime deletedAt;

  @PrePersist
  void prePersist() {
    if (deletedAt == null) {
      deletedAt = OffsetDateTime.now();
    }
  }
}
