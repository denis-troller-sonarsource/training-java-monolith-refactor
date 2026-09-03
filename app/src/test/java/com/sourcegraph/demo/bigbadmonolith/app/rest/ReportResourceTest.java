package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerBillReport;
import com.sourcegraph.demo.bigbadmonolith.billing.repository.JdbcReportRepository;
import com.sourcegraph.demo.bigbadmonolith.billing.service.DefaultReportService;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReportResourceTest extends RestResourceTestBase {

    private ReportResource resource;
    private Long customerId;

    @BeforeEach
    void createResourceAndFixtures() {
        resource = new ReportResource(new DefaultReportService(new JdbcReportRepository()));
        customerId = customerService.createCustomer(
            new Customer("Acme Corp", "billing@acme.com", "123 Business St")).getId();
        Long userId = userService.createUser(new User("john@example.com", "John")).getId();
        Long categoryId = categoryService.createCategory(
            new BillingCategory("Development", "work", new BigDecimal("150.00"))).getId();
        billableHourService.logHour(new BillableHour(
            customerId, userId, categoryId, new BigDecimal("8.00"), "note", LocalDate.of(2024, 6, 15)));
    }

    @Test
    void customerBillReturnsReport() {
        CustomerBillReport report = resource.customerBill(customerId);

        assertThat(report.customerName()).isEqualTo("Acme Corp");
        assertThat(report.lines()).hasSize(1);
    }

    @Test
    void monthlyReturns400WhenParamsMissing() {
        Response response = resource.monthly(null, null);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void monthlyReturnsSummaryWhenParamsPresent() {
        Response response = resource.monthly(2024, 6);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    void revenueByCustomerReturnsRows() {
        assertThat(resource.revenueByCustomer()).hasSize(1);
    }

    @Test
    void revenueByCategoryReturnsRows() {
        assertThat(resource.revenueByCategory()).hasSize(1);
    }
}
