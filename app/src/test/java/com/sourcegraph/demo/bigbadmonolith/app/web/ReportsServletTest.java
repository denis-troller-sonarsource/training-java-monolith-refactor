package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletRequest;
import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletResponse;
import com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerBillReport;
import com.sourcegraph.demo.bigbadmonolith.billing.api.MonthlySummaryRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.RevenueByCategoryRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.RevenueByCustomerRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportsServletTest extends ServletTestBase {

    private ReportsServlet servlet;
    private Long customerId;

    @BeforeEach
    void createServletAndFixtures() {
        servlet = new ReportsServlet(reportService, customerService);
        customerId = seedCustomer("Acme");
        Long userId = seedUser("John");
        Long categoryId = seedCategory("Dev", "100.00");
        seedHour(customerId, userId, categoryId, "8.00", LocalDate.of(2024, 6, 15));
    }

    @Test
    @SuppressWarnings("unchecked")
    void doGetWithNoReportTypeForwardsWithCustomersOnly() {
        FakeHttpServletRequest request = newRequest();

        servlet.doGet(request, new FakeHttpServletResponse());

        assertThat(request.getForwardedPath()).isEqualTo("/WEB-INF/views/reports.jsp");
        assertThat(request.getAttribute("reportType")).isNull();
        assertThat((List<Object>) request.getAttribute("customers")).hasSize(1);
    }

    @Test
    void doGetCustomerBillSetsCustomerBillAttribute() {
        FakeHttpServletRequest request = newRequest()
            .withParameter("reportType", "customer")
            .withParameter("customerId", customerId.toString());

        servlet.doGet(request, new FakeHttpServletResponse());

        CustomerBillReport bill = (CustomerBillReport) request.getAttribute("customerBill");
        assertThat(bill.customerName()).isEqualTo("Acme");
        assertThat(request.getAttribute("selectedCustomerId")).isEqualTo(customerId.toString());
    }

    @Test
    void doGetCustomerBillWithoutIdSkipsBill() {
        FakeHttpServletRequest request = newRequest().withParameter("reportType", "customer");

        servlet.doGet(request, new FakeHttpServletResponse());

        assertThat(request.getAttribute("customerBill")).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void doGetMonthlySetsRowsAndTotals() {
        FakeHttpServletRequest request = newRequest()
            .withParameter("reportType", "monthly")
            .withParameter("year", "2024")
            .withParameter("month", "6");

        servlet.doGet(request, new FakeHttpServletResponse());

        List<MonthlySummaryRow> rows =
            (List<MonthlySummaryRow>) request.getAttribute("monthlyRows");
        assertThat(rows).hasSize(1);
        assertThat((BigDecimal) request.getAttribute("monthlyTotalHours"))
            .isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat((BigDecimal) request.getAttribute("monthlyTotalAmount"))
            .isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    void doGetMonthlyWithMissingParamsSkipsRows() {
        FakeHttpServletRequest request = newRequest().withParameter("reportType", "monthly");

        servlet.doGet(request, new FakeHttpServletResponse());

        assertThat(request.getAttribute("monthlyRows")).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void doGetRevenueSetsBothRevenueBreakdowns() {
        FakeHttpServletRequest request = newRequest().withParameter("reportType", "revenue");

        servlet.doGet(request, new FakeHttpServletResponse());

        assertThat((List<RevenueByCustomerRow>) request.getAttribute("revenueByCustomer"))
            .hasSize(1);
        assertThat((List<RevenueByCategoryRow>) request.getAttribute("revenueByCategory"))
            .hasSize(1);
    }
}
