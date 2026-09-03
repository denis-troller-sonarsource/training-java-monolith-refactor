package com.sourcegraph.demo.bigbadmonolith.billing.api;

import java.math.BigDecimal;

/**
 * One row of the by-customer revenue report: total hours, total revenue and the average hourly rate
 * across the customer's billable hours.
 */
public record RevenueByCustomerRow(
    String customerName,
    BigDecimal totalHours,
    BigDecimal totalRevenue,
    BigDecimal averageRate) {
}
