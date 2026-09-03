package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletRequest;
import com.sourcegraph.demo.bigbadmonolith.billing.api.BillingService;
import com.sourcegraph.demo.bigbadmonolith.billing.api.ReportService;
import com.sourcegraph.demo.bigbadmonolith.billing.repository.JdbcReportRepository;
import com.sourcegraph.demo.bigbadmonolith.billing.service.DefaultBillingService;
import com.sourcegraph.demo.bigbadmonolith.billing.service.DefaultReportService;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.catalog.repository.JdbcBillingCategoryRepository;
import com.sourcegraph.demo.bigbadmonolith.catalog.service.DefaultBillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;
import com.sourcegraph.demo.bigbadmonolith.customers.repository.JdbcCustomerRepository;
import com.sourcegraph.demo.bigbadmonolith.customers.service.DefaultCustomerService;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourService;
import com.sourcegraph.demo.bigbadmonolith.timesheet.repository.JdbcBillableHourRepository;
import com.sourcegraph.demo.bigbadmonolith.timesheet.service.DefaultBillableHourService;
import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;
import com.sourcegraph.demo.bigbadmonolith.users.repository.JdbcUserRepository;
import com.sourcegraph.demo.bigbadmonolith.users.service.DefaultUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Shared setup for the MVC servlet tests: installs an {@link InMemoryDatabase} so the servlets run
 * against the real service/repository stack (which also re-covers the service impls), builds all the
 * context services plus the billing report/billing services, and provides small helpers to seed
 * fixtures and to build a {@link FakeHttpServletRequest}. Factored out so the six servlet test
 * classes share this block instead of duplicating it.
 */
abstract class ServletTestBase {

    private InMemoryDatabase db;

    protected UserService userService;
    protected CustomerService customerService;
    protected BillingCategoryService categoryService;
    protected BillableHourService billableHourService;
    protected ReportService reportService;
    protected BillingService billingService;

    @BeforeEach
    void installDatabase() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        userService = new DefaultUserService(new JdbcUserRepository());
        customerService = new DefaultCustomerService(new JdbcCustomerRepository());
        categoryService = new DefaultBillingCategoryService(new JdbcBillingCategoryRepository());
        billableHourService = new DefaultBillableHourService(new JdbcBillableHourRepository());
        reportService = new DefaultReportService(new JdbcReportRepository());
        billingService =
            new DefaultBillingService(billableHourService, categoryService, customerService);
    }

    @AfterEach
    void closeDatabase() {
        db.close();
    }

    /** A fresh request with no parameters and an empty session. */
    protected FakeHttpServletRequest newRequest() {
        return new FakeHttpServletRequest();
    }

    protected Long seedCustomer(String name) {
        return customerService.createCustomer(
            new Customer(name, name + "@example.com", "1 Main St")).getId();
    }

    protected Long seedUser(String name) {
        return userService.createUser(new User(name + "@example.com", name)).getId();
    }

    protected Long seedCategory(String name, String rate) {
        return categoryService.createCategory(
            new BillingCategory(name, "desc", new BigDecimal(rate))).getId();
    }

    protected void seedHour(Long customerId, Long userId, Long categoryId, String hours,
                            LocalDate date) {
        billableHourService.logHour(new BillableHour(
            customerId, userId, categoryId, new BigDecimal(hours), "note", date));
    }
}
