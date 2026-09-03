package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourService;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.Timesheet;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.Catalog;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customers;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;
import com.sourcegraph.demo.bigbadmonolith.users.api.Users;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization tests for {@link DataInitializationService}. These lock in the seeding
 * behavior and its idempotency guard before the modular refactoring begins.
 */
class DataInitializationServiceTest {

    private InMemoryDatabase db;
    private DataInitializationService service;

    private UserService userService;
    private CustomerService customerService;
    private BillingCategoryService categoryService;
    private BillableHourService billableHourService;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        service = new DataInitializationService();
        userService = Users.service();
        customerService = Customers.service();
        categoryService = Catalog.service();
        billableHourService = Timesheet.service();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void initializeSampleDataSeedsExpectedCounts() throws SQLException {
        service.initializeSampleData();

        assertThat(userService.listUsers()).hasSize(2);
        assertThat(customerService.listCustomers()).hasSize(3);
        assertThat(categoryService.listCategories()).hasSize(3);
        assertThat(billableHourService.listHours()).hasSize(6);
    }

    @Test
    void initializeSampleDataIsIdempotent() throws SQLException {
        service.initializeSampleData();
        service.initializeSampleData();

        assertThat(userService.listUsers()).hasSize(2);
        assertThat(customerService.listCustomers()).hasSize(3);
        assertThat(categoryService.listCategories()).hasSize(3);
        assertThat(billableHourService.listHours()).hasSize(6);
    }
}
