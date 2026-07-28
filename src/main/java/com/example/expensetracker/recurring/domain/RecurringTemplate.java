package com.example.expensetracker.recurring.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "recurring_templates")
public class RecurringTemplate {

  public enum ScheduleType {
    MONTHLY
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 64)
  private String category;

  @Column(length = 255)
  private String note;

  @Enumerated(EnumType.STRING)
  @Column(name = "schedule_type", nullable = false, length = 16)
  private ScheduleType scheduleType;

  @Column(name = "day_of_month")
  private Integer dayOfMonth;

  @Column(name = "next_run_date", nullable = false)
  private LocalDate nextRunDate;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
  private OffsetDateTime updatedAt;
}
