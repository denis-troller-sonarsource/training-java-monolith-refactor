package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletRequest;
import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletResponse;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerServletTest extends ServletTestBase {

    private CustomerServlet servlet;

    @BeforeEach
    void createServlet() {
        servlet = new CustomerServlet(customerService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void doGetForwardsToCustomersViewWithCustomerList() {
        seedCustomer("Acme");

        FakeHttpServletRequest request = newRequest();

        servlet.doGet(request, new FakeHttpServletResponse());

        assertThat(request.getForwardedPath()).isEqualTo("/WEB-INF/views/customers.jsp");
        List<Customer> customers = (List<Customer>) request.getAttribute("customers");
        assertThat(customers).hasSize(1);
    }

    @Test
    void doPostAddCreatesCustomerAndRedirects() {
        FakeHttpServletRequest request = newRequest()
            .withParameter("action", "add")
            .withParameter("name", "Beta LLC")
            .withParameter("email", "beta@example.com")
            .withParameter("address", "5 Side St");
        FakeHttpServletResponse response = new FakeHttpServletResponse();

        servlet.doPost(request, response);

        assertThat(response.getRedirectLocation()).isEqualTo("/customers");
        assertThat(customerService.listCustomers()).hasSize(1);
        assertThat(request.getSession().getAttribute(ViewSupport.FLASH_MESSAGE))
            .isEqualTo("Customer added successfully!");
    }

    @Test
    void doPostDeleteRemovesCustomerAndRedirects() {
        Long customerId = seedCustomer("Acme");
        FakeHttpServletRequest request = newRequest()
            .withParameter("action", "delete")
            .withParameter("id", customerId.toString());
        FakeHttpServletResponse response = new FakeHttpServletResponse();

        servlet.doPost(request, response);

        assertThat(response.getRedirectLocation()).isEqualTo("/customers");
        assertThat(customerService.listCustomers()).isEmpty();
    }

    @Test
    void doPostDeleteWithInvalidIdSetsErrorFlash() {
        FakeHttpServletRequest request = newRequest()
            .withParameter("action", "delete")
            .withParameter("id", "not-a-number");

        servlet.doPost(request, new FakeHttpServletResponse());

        assertThat((Boolean) request.getSession().getAttribute(ViewSupport.FLASH_ERROR)).isTrue();
        assertThat(request.getSession().getAttribute(ViewSupport.FLASH_MESSAGE))
            .isEqualTo("Error: Invalid customer ID format");
    }
}
