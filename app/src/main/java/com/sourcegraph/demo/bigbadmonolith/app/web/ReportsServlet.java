package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.billing.api.MonthlySummaryRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.ReportService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reports controller. GET reads the {@code reportType} / {@code customerId} / {@code month} /
 * {@code year} query parameters, calls the appropriate {@link ReportService} method(s) and sets the
 * results as request attributes for the pure-JSTL reports view. The customer dropdown is populated
 * from {@link CustomerService}. All report data comes from the billing module (no in-view logic).
 */
@WebServlet("/reports")
@Dependent
public class ReportsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient ReportService reportService;
    private final transient CustomerService customerService;

    @Inject
    public ReportsServlet(ReportService reportService, CustomerService customerService) {
        this.reportService = reportService;
        this.customerService = customerService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String reportType = request.getParameter("reportType");
        request.setAttribute("reportType", reportType);
        try {
            request.setAttribute("customers", customerService.listCustomers());
            if ("customer".equals(reportType)) {
                prepareCustomerBill(request);
            } else if ("monthly".equals(reportType)) {
                prepareMonthly(request);
            } else if ("revenue".equals(reportType)) {
                request.setAttribute("revenueByCustomer", reportService.revenueByCustomer());
                request.setAttribute("revenueByCategory", reportService.revenueByCategory());
            }
        } catch (RuntimeException e) {
            request.setAttribute("reportError", "Error generating report: " + e.getMessage());
        }
        ViewSupport.render(request, response, "/WEB-INF/views/reports.jsp");
    }

    private void prepareCustomerBill(HttpServletRequest request) {
        String customerId = request.getParameter("customerId");
        request.setAttribute("selectedCustomerId", customerId);
        if (customerId != null && !customerId.trim().isEmpty()) {
            request.setAttribute("customerBill",
                reportService.customerBill(Long.valueOf(customerId.trim())));
        }
    }

    private void prepareMonthly(HttpServletRequest request) {
        String year = request.getParameter("year");
        String month = request.getParameter("month");
        request.setAttribute("selectedYear", year);
        request.setAttribute("selectedMonth", month);
        if (year != null && month != null) {
            List<MonthlySummaryRow> rows =
                reportService.monthlySummary(Integer.parseInt(year), Integer.parseInt(month));
            request.setAttribute("monthlyRows", rows);

            BigDecimal totalHours = BigDecimal.ZERO;
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (MonthlySummaryRow row : rows) {
                totalHours = totalHours.add(row.totalHours());
                totalAmount = totalAmount.add(row.totalAmount());
            }
            request.setAttribute("monthlyTotalHours", totalHours);
            request.setAttribute("monthlyTotalAmount", totalAmount);
        }
    }
}
