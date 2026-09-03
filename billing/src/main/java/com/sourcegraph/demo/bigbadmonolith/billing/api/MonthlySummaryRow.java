package com.sourcegraph.demo.bigbadmonolith.billing.api;

import java.math.BigDecimal;

/**
 * One row of the monthly summary report: per-customer total hours and total amount for a month.
 */
public record MonthlySummaryRow(
    String customerName,
    BigDecimal totalHours,
    BigDecimal totalAmount) {
}
