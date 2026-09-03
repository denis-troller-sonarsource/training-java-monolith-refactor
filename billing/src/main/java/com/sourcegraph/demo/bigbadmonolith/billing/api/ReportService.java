package com.sourcegraph.demo.bigbadmonolith.billing.api;

import java.util.List;

/**
 * Reporting service for the billing context. Produces the three billing reports (customer bill,
 * monthly summary and revenue summary) that were previously assembled with raw JDBC in the web layer.
 * The public entry point for the web layer.
 */
public interface ReportService {

    /**
     * Builds a bill for a customer: the customer's name/email, one line per billable hour (joined to
     * user and category), and the {@code totalHours}/{@code totalAmount} running totals.
     */
    CustomerBillReport customerBill(Long customerId);

    /**
     * Per-customer totals (hours and amount) for the given year/month, ordered by amount descending.
     * The month range is inclusive of the real last day of the month.
     */
    List<MonthlySummaryRow> monthlySummary(int year, int month);

    /** Per-customer revenue summary across all time, ordered by revenue descending. */
    List<RevenueByCustomerRow> revenueByCustomer();

    /** Per-category revenue summary across all time, ordered by revenue descending. */
    List<RevenueByCategoryRow> revenueByCategory();
}
