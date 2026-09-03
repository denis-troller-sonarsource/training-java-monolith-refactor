package com.sourcegraph.demo.bigbadmonolith.billing.api;

import java.math.BigDecimal;

/**
 * One row of the by-user revenue rollup: the user's id, name and email, and the total hours and
 * revenue logged against that user across all billable hours.
 */
public record UserRevenueRow(
    Long userId,
    String userName,
    String userEmail,
    BigDecimal totalHours,
    BigDecimal totalRevenue) {

    // JavaBean-style getters so Jakarta Pages EL (BeanELResolver, which only recognizes getX()
    // accessors, not record components) can render these rows in the JSP views.
    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
