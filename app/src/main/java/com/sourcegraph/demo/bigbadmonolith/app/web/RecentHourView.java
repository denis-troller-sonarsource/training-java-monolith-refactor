package com.sourcegraph.demo.bigbadmonolith.app.web;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * View model for one row of the "Recent Hours" table on the log-hours page: a billable hour joined
 * to its customer/user/category display names, with the line total. Assembled by
 * {@link HoursServlet} so the JSP can render it with plain EL (no lookups, no scriptlets).
 */
public record RecentHourView(
    LocalDate dateLogged,
    String customerName,
    String userName,
    String categoryName,
    BigDecimal hours,
    BigDecimal hourlyRate,
    BigDecimal lineTotal,
    String note) {

    // JavaBean-style getters so Jakarta Pages EL (BeanELResolver, which only recognizes getX()
    // accessors, not record components) can render these rows in hours.jsp.
    public LocalDate getDateLogged() {
        return dateLogged;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getUserName() {
        return userName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public String getNote() {
        return note;
    }
}
