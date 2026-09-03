package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerResourceTest extends RestResourceTestBase {

    private CustomerResource resource;

    @BeforeEach
    void createResource() {
        resource = new CustomerResource(customerService);
    }

    private Customer seedCustomer() {
        return customerService.createCustomer(new Customer("Acme Corp", "billing@acme.com", "123 Business St"));
    }

    @Test
    void listReturnsAllCustomers() {
        seedCustomer();

        assertThat(resource.list()).hasSize(1);
    }

    @Test
    void getReturnsCustomerWhenFound() {
        Customer saved = seedCustomer();

        Response response = resource.get(saved.getId());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Customer.class);
        assertThat(((Customer) response.getEntity()).getName()).isEqualTo("Acme Corp");
    }

    @Test
    void getReturns404WhenMissing() {
        Response response = resource.get(999L);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void createReturns201WithSavedEntity() {
        Response response = resource.create(new Customer("New Co", "new@co.com", "1 New St"));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        Customer saved = (Customer) response.getEntity();
        assertThat(saved.getId()).isNotNull();
        assertThat(customerService.listCustomers()).hasSize(1);
    }

    @Test
    void updateReturnsOkWhenFound() {
        Customer saved = seedCustomer();
        Customer changes = new Customer("Renamed", "billing@acme.com", "123 Business St");

        Response response = resource.update(saved.getId(), changes);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(customerService.getCustomer(saved.getId()).getName()).isEqualTo("Renamed");
    }

    @Test
    void updateReturns404WhenMissing() {
        Response response = resource.update(999L, new Customer("X", "x@x.com", "x"));

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteReturnsNoContentWhenFound() {
        Customer saved = seedCustomer();

        Response response = resource.delete(saved.getId());

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        assertThat(customerService.listCustomers()).isEmpty();
    }

    @Test
    void deleteReturns404WhenMissing() {
        Response response = resource.delete(999L);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
