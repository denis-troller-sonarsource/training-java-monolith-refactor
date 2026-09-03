package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourService;
import com.sourcegraph.demo.bigbadmonolith.timesheet.repository.JdbcBillableHourRepository;
import com.sourcegraph.demo.bigbadmonolith.timesheet.service.DefaultBillableHourService;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.catalog.repository.JdbcBillingCategoryRepository;
import com.sourcegraph.demo.bigbadmonolith.catalog.service.DefaultBillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;
import com.sourcegraph.demo.bigbadmonolith.customers.repository.JdbcCustomerRepository;
import com.sourcegraph.demo.bigbadmonolith.customers.service.DefaultCustomerService;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;
import com.sourcegraph.demo.bigbadmonolith.users.repository.JdbcUserRepository;
import com.sourcegraph.demo.bigbadmonolith.users.service.DefaultUserService;
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
        userService = new DefaultUserService(new JdbcUserRepository());
        customerService = new DefaultCustomerService(new JdbcCustomerRepository());
        categoryService = new DefaultBillingCategoryService(new JdbcBillingCategoryRepository());
        billableHourService = new DefaultBillableHourService(new JdbcBillableHourRepository());
        service = new DataInitializationService(
            userService, customerService, categoryService, billableHourService);
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void initializeSampleDataSeedsExpectedCounts() {
        service.initializeSampleData();

        assertThat(userService.listUsers()).hasSize(2);
        assertThat(customerService.listCustomers()).hasSize(3);
        assertThat(categoryService.listCategories()).hasSize(3);
        assertThat(billableHourService.listHours()).hasSize(6);
    }

    @Test
    void initializeSampleDataIsIdempotent() {
        service.initializeSampleData();
        service.initializeSampleData();

        assertThat(userService.listUsers()).hasSize(2);
        assertThat(customerService.listCustomers()).hasSize(3);
        assertThat(categoryService.listCategories()).hasSize(3);
        assertThat(billableHourService.listHours()).hasSize(6);
    }

    @Test
    void injectedConstructorSeedsWithSuppliedServices() {
        DataInitializationService injected = new DataInitializationService(
            userService, customerService, categoryService, billableHourService);

        injected.initializeSampleData();

        assertThat(userService.listUsers()).hasSize(2);
        assertThat(customerService.listCustomers()).hasSize(3);
        assertThat(categoryService.listCategories()).hasSize(3);
        assertThat(billableHourService.listHours()).hasSize(6);
    }
}
