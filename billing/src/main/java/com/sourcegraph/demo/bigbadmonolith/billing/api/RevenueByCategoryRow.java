package com.sourcegraph.demo.bigbadmonolith.billing.api;

import java.math.BigDecimal;

/**
 * One row of the by-category revenue report: the category name, its hourly rate, and the total hours
 * and revenue logged against it.
 */
public record RevenueByCategoryRow(
    String categoryName,
    BigDecimal hourlyRate,
    BigDecimal totalHours,
    BigDecimal totalRevenue) {
}
