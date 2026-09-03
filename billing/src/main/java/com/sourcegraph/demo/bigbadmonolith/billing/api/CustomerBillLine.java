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
}
