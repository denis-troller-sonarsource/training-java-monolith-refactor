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

    // JavaBean-style getters so Jakarta Pages EL can render these records in the JSP views.
    public String getCustomerName() { return customerName; }
    public BigDecimal getTotalHours() { return totalHours; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getAverageRate() { return averageRate; }
}
