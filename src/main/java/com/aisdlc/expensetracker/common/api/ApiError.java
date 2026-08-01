package com.aisdlc.expensetracker.common.api;

import java.time.Instant;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

  private Instant timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
  private List<FieldViolation> fieldViolations;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FieldViolation {
    private String field;
    private String message;
  }
}
