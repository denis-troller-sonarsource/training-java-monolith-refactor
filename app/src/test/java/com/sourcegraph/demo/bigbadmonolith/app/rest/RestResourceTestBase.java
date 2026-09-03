package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.catalog.repository.JdbcBillingCategoryRepository;
import com.sourcegraph.demo.bigbadmonolith.catalog.service.DefaultBillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;
import com.sourcegraph.demo.bigbadmonolith.customers.repository.JdbcCustomerRepository;
import com.sourcegraph.demo.bigbadmonolith.customers.service.DefaultCustomerService;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourService;
import com.sourcegraph.demo.bigbadmonolith.timesheet.repository.JdbcBillableHourRepository;
import com.sourcegraph.demo.bigbadmonolith.timesheet.service.DefaultBillableHourService;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;
import com.sourcegraph.demo.bigbadmonolith.users.repository.JdbcUserRepository;
import com.sourcegraph.demo.bigbadmonolith.users.service.DefaultUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.SQLException;

/**
 * Shared setup for the REST resource tests: installs an {@link InMemoryDatabase} so the resources
 * run against the real service/repository stack, and exposes the context services (resolved via
 * their {@code api} factories) so subclasses can construct resources and seed fixtures.
 */
abstract class RestResourceTestBase {

    private InMemoryDatabase db;

    protected UserService userService;
    protected CustomerService customerService;
    protected BillingCategoryService categoryService;
    protected BillableHourService billableHourService;

    @BeforeEach
    void installDatabase() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        userService = new DefaultUserService(new JdbcUserRepository());
        customerService = new DefaultCustomerService(new JdbcCustomerRepository());
        categoryService = new DefaultBillingCategoryService(new JdbcBillingCategoryRepository());
        billableHourService = new DefaultBillableHourService(new JdbcBillableHourRepository());
    }

    @AfterEach
    void closeDatabase() {
        db.close();
    }
}
