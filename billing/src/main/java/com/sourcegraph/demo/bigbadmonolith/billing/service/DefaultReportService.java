package com.sourcegraph.demo.bigbadmonolith.billing.service;

import com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerBillLine;
import com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerBillReport;
import com.sourcegraph.demo.bigbadmonolith.billing.api.MonthlySummaryRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.ReportService;
import com.sourcegraph.demo.bigbadmonolith.billing.api.RevenueByCategoryRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.RevenueByCustomerRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.UserRevenueRow;
import com.sourcegraph.demo.bigbadmonolith.billing.repository.JdbcReportRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Default {@link ReportService}. Reads report rows from {@link JdbcReportRepository} and computes the
 * customer-bill running totals in memory. Extracted from the raw-JDBC {@code reports.jsp}.
 */
@ApplicationScoped
public class DefaultReportService implements ReportService {

    private final JdbcReportRepository repository;

    @Inject
    public DefaultReportService(JdbcReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public CustomerBillReport customerBill(Long customerId) {
        JdbcReportRepository.CustomerContact contact = repository.findCustomerContact(customerId);
        String customerName = contact == null ? "" : contact.name();
        String customerEmail = contact == null ? "" : contact.email();

        List<CustomerBillLine> lines = repository.findBillLines(customerId);

        BigDecimal totalHours = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CustomerBillLine line : lines) {
            totalHours = totalHours.add(line.hours());
            totalAmount = totalAmount.add(line.lineTotal());
        }

        return new CustomerBillReport(customerName, customerEmail, lines, totalHours, totalAmount);
    }

    @Override
    public List<MonthlySummaryRow> monthlySummary(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        // Correct month-end (the legacy reports.jsp used a naive year-month-31 which was wrong for short months)
        LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
        return repository.monthlySummary(startDate, endDate);
    }

    @Override
    public List<RevenueByCustomerRow> revenueByCustomer() {
        return repository.revenueByCustomer();
    }

    @Override
    public List<RevenueByCategoryRow> revenueByCategory() {
        return repository.revenueByCategory();
    }

    @Override
    public BigDecimal totalRevenue() {
        return repository.totalRevenue();
    }

    @Override
    public List<UserRevenueRow> revenueByUser() {
        return repository.revenueByUser();
    }
}
