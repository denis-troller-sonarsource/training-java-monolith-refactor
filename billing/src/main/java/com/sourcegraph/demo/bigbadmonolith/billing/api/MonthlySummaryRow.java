package com.sourcegraph.demo.bigbadmonolith.billing.api;

import java.math.BigDecimal;

/**
 * One row of the monthly summary report: per-customer total hours and total amount for a month.
 */
public record MonthlySummaryRow(
    String customerName,
    BigDecimal totalHours,
    BigDecimal totalAmount) {

    // JavaBean-style getters so Jakarta Pages EL can render these records in the JSP views.
    public String getCustomerName() { return customerName; }
    public BigDecimal getTotalHours() { return totalHours; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
