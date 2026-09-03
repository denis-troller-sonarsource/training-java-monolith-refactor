package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.Catalog;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customers;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourService;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.Timesheet;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;
import com.sourcegraph.demo.bigbadmonolith.users.api.Users;
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
        userService = Users.service();
        customerService = Customers.service();
        categoryService = Catalog.service();
        billableHourService = Timesheet.service();
    }

    @AfterEach
    void closeDatabase() {
        db.close();
    }
}
