package com.sourcegraph.demo.bigbadmonolith.billing.api;

import java.math.BigDecimal;
import java.util.List;

/**
 * A customer bill: the customer's name and email, the individual billable-hour {@code lines}, and
 * the running totals ({@code totalHours}, {@code totalAmount}) summed from those lines.
 */
public record CustomerBillReport(
    String customerName,
    String customerEmail,
    List<CustomerBillLine> lines,
    BigDecimal totalHours,
    BigDecimal totalAmount) {

    // JavaBean-style getters so Jakarta Pages EL can render these records in the JSP views.
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public List<CustomerBillLine> getLines() { return lines; }
    public BigDecimal getTotalHours() { return totalHours; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
