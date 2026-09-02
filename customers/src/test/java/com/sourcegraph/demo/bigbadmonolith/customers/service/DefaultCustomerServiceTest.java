package com.sourcegraph.demo.bigbadmonolith.customers.service;

import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link DefaultCustomerService} end to end against the JDBC repository and in-memory Derby,
 * covering the service delegation for every operation.
 */
class DefaultCustomerServiceTest {

    private InMemoryDatabase db;
    private CustomerService service;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        service = new DefaultCustomerService();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void createCustomerAssignsId() {
        Customer saved = service.createCustomer(new Customer("Acme Corp", "billing@acme.test", "1 Road"));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void getCustomerReturnsCreatedCustomer() {
        Customer saved = service.createCustomer(new Customer("Acme Corp", "billing@acme.test", "1 Road"));

        assertThat(service.getCustomer(saved.getId()).getName()).isEqualTo("Acme Corp");
    }

    @Test
    void listCustomersReturnsAll() {
        service.createCustomer(new Customer("Acme Corp", "billing@acme.test", "1 Road"));
        service.createCustomer(new Customer("TechStart Inc", "finance@techstart.test", "2 Ave"));

        List<Customer> all = service.listCustomers();

        assertThat(all).extracting(Customer::getName)
            .containsExactlyInAnyOrder("Acme Corp", "TechStart Inc");
    }

    @Test
    void updateCustomerChangesName() {
        Customer saved = service.createCustomer(new Customer("Acme Corp", "billing@acme.test", "1 Road"));
        saved.setName("Acme Renamed");

        boolean updated = service.updateCustomer(saved);

        assertThat(updated).isTrue();
        assertThat(service.getCustomer(saved.getId()).getName()).isEqualTo("Acme Renamed");
    }

    @Test
    void deleteCustomerRemovesCustomer() {
        Customer saved = service.createCustomer(new Customer("Acme Corp", "billing@acme.test", "1 Road"));

        boolean deleted = service.deleteCustomer(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(service.getCustomer(saved.getId())).isNull();
    }
}
