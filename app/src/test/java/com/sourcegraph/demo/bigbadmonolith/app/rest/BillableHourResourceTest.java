package com.sourcegraph.demo.bigbadmonolith.app.rest;

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

class BillableHourResourceTest extends RestResourceTestBase {

    private BillableHourResource resource;
    private Long customerId;
    private Long userId;
    private Long categoryId;

    @BeforeEach
    void createResourceAndFixtures() {
        resource = new BillableHourResource(billableHourService);
        customerId = customerService.createCustomer(
            new Customer("Acme Corp", "billing@acme.com", "123 Business St")).getId();
        userId = userService.createUser(new User("john.doe@example.com", "John Doe")).getId();
        categoryId = categoryService.createCategory(
            new BillingCategory("Development", "work", new BigDecimal("150.00"))).getId();
    }

    private BillableHour seedHour(Long forCustomer, Long forUser) {
        return billableHourService.logHour(new BillableHour(
            forCustomer, forUser, categoryId, new BigDecimal("8.00"), "note", LocalDate.now()));
    }

    @Test
    void listReturnsAllHours() {
        seedHour(customerId, userId);

        assertThat(resource.list(null, null)).hasSize(1);
    }

    @Test
    void listFiltersByCustomerId() {
        Long otherCustomer = customerService.createCustomer(
            new Customer("Other", "o@o.com", "addr")).getId();
        seedHour(customerId, userId);
        seedHour(otherCustomer, userId);

        assertThat(resource.list(customerId, null)).hasSize(1);
    }

    @Test
    void listFiltersByUserId() {
        Long otherUser = userService.createUser(new User("jane@example.com", "Jane")).getId();
        seedHour(customerId, userId);
        seedHour(customerId, otherUser);

        assertThat(resource.list(null, userId)).hasSize(1);
    }

    @Test
    void getReturnsHourWhenFound() {
        BillableHour saved = seedHour(customerId, userId);

        Response response = resource.get(saved.getId());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(((BillableHour) response.getEntity()).getNote()).isEqualTo("note");
    }

    @Test
    void getReturns404WhenMissing() {
        Response response = resource.get(999L);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void createReturns201WithSavedEntity() {
        Response response = resource.create(new BillableHour(
            customerId, userId, categoryId, new BigDecimal("4.00"), "new", LocalDate.now()));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(((BillableHour) response.getEntity()).getId()).isNotNull();
    }

    @Test
    void updateReturnsOkWhenFound() {
        BillableHour saved = seedHour(customerId, userId);
        BillableHour changes = new BillableHour(
            customerId, userId, categoryId, new BigDecimal("2.00"), "changed", LocalDate.now());

        Response response = resource.update(saved.getId(), changes);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(billableHourService.getHour(saved.getId()).getNote()).isEqualTo("changed");
    }

    @Test
    void updateReturns404WhenMissing() {
        BillableHour changes = new BillableHour(
            customerId, userId, categoryId, new BigDecimal("2.00"), "x", LocalDate.now());

        Response response = resource.update(999L, changes);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteReturnsNoContentWhenFound() {
        BillableHour saved = seedHour(customerId, userId);

        Response response = resource.delete(saved.getId());

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        assertThat(billableHourService.listHours()).isEmpty();
    }

    @Test
    void deleteReturns404WhenMissing() {
        Response response = resource.delete(999L);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
