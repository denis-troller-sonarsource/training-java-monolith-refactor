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

    // JavaBean-style getters so Jakarta Pages EL can render these records in the JSP views.
    public String getCategoryName() { return categoryName; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public BigDecimal getTotalHours() { return totalHours; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
}
