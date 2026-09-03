package com.sourcegraph.demo.bigbadmonolith.billing.api;

import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHour;

import java.util.Map;

/**
 * Billing and reporting service. Composes the customers, catalog and timesheet contexts (via their
 * api packages) to produce bills, reports and validation. The public entry point for the web layer.
 */
public interface BillingService {

    /**
     * Builds a bill for a customer: {@code customer}, {@code billableHours}, {@code totalHours},
     * {@code totalAmount}, {@code generatedDate}. Throws if the customer does not exist.
     */
    Map<String, Object> generateCustomerBill(Long customerId);

    /**
     * Builds a revenue report for the given year/month: {@code year}, {@code month},
     * {@code totalRevenue}, {@code totalHours}, {@code revenueByCategory}, {@code generatedDate}.
     */
    Map<String, Object> generateMonthlyReport(int year, int month);

    /** Returns accumulated validation messages for a billable hour ("" when valid). */
    String validateBillableHour(BillableHour hour);
}
