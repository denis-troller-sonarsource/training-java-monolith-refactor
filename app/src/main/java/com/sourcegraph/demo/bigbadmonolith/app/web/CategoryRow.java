package com.sourcegraph.demo.bigbadmonolith.app.web;

import java.math.BigDecimal;

/**
 * View model for one row of the categories table: the category id/name/description/rate together
 * with its revenue rollup (total hours + revenue). Merges the catalog {@code BillingCategory} (for
 * id/description, needed by the update-rate form) with the billing module's per-category revenue
 * rollup, so the JSP renders with plain EL and no in-view computation.
 */
public record CategoryRow(
    Long id,
    String name,
    String description,
    BigDecimal hourlyRate,
    BigDecimal totalHours,
    BigDecimal totalRevenue) {

    // JavaBean-style getters so Jakarta Pages EL (BeanELResolver, which only recognizes getX()
    // accessors, not record components) can render these rows in categories.jsp.
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
