package com.aisdlc.expensetracker.report.dto;

import java.math.BigDecimal;

public record CategoryBreakdownItem(Long categoryId, String categoryName, BigDecimal total) {
}
