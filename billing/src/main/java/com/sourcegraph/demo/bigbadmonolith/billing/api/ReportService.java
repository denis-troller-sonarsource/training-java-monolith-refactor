package com.sourcegraph.demo.bigbadmonolith.billing.api;

import java.math.BigDecimal;
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

    /**
     * Total revenue across every billable hour (sum of {@code hours * hourly_rate}). Used by the
     * dashboard. Returns {@link BigDecimal#ZERO} when there are no billable hours.
     */
    BigDecimal totalRevenue();

    /**
     * Per-user revenue rollup across all time: each user with the total hours and total revenue
     * logged against them, ordered by revenue descending. Users with no hours appear with zeros.
     */
    List<UserRevenueRow> revenueByUser();
}
