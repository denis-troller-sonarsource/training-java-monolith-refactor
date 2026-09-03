package com.sourcegraph.demo.bigbadmonolith.billing.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One billable-hour line on a customer bill: the logged date, who logged it, the billing category,
 * the hours, the category's hourly rate, the computed line total ({@code hours * hourlyRate}) and
 * the free-text note.
 */
public record CustomerBillLine(
    LocalDate dateLogged,
    String userName,
    String categoryName,
    BigDecimal hours,
    BigDecimal hourlyRate,
    BigDecimal lineTotal,
    String note) {

    // JavaBean-style getters so Jakarta Pages EL can render these records in the JSP views.
    public LocalDate getDateLogged() { return dateLogged; }
    public String getUserName() { return userName; }
    public String getCategoryName() { return categoryName; }
    public BigDecimal getHours() { return hours; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public String getNote() { return note; }
}
