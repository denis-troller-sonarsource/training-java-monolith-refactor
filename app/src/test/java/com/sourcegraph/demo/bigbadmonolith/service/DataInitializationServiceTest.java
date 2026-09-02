package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.dao.BillableHourDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.BillingCategoryDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.CustomerDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.UserDAO;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
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

    private UserDAO userDAO;
    private CustomerDAO customerDAO;
    private BillingCategoryDAO categoryDAO;
    private BillableHourDAO billableHourDAO;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        service = new DataInitializationService();
        userDAO = new UserDAO();
        customerDAO = new CustomerDAO();
        categoryDAO = new BillingCategoryDAO();
        billableHourDAO = new BillableHourDAO();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void initializeSampleDataSeedsExpectedCounts() throws SQLException {
        service.initializeSampleData();

        assertThat(userDAO.findAll()).hasSize(2);
        assertThat(customerDAO.findAll()).hasSize(3);
        assertThat(categoryDAO.findAll()).hasSize(3);
        assertThat(billableHourDAO.findAll()).hasSize(6);
    }

    @Test
    void initializeSampleDataIsIdempotent() throws SQLException {
        service.initializeSampleData();
        service.initializeSampleData();

        assertThat(userDAO.findAll()).hasSize(2);
        assertThat(customerDAO.findAll()).hasSize(3);
        assertThat(categoryDAO.findAll()).hasSize(3);
        assertThat(billableHourDAO.findAll()).hasSize(6);
    }
}
