package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerNotFoundException;
import com.sourcegraph.demo.bigbadmonolith.billing.service.DefaultBillingService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BillResourceTest extends RestResourceTestBase {

    private BillResource resource;

    @BeforeEach
    void createResource() {
        resource = new BillResource(
            new DefaultBillingService(billableHourService, categoryService, customerService));
    }

    @Test
    void generateBillReturnsMapForExistingCustomer() {
        Long customerId = customerService.createCustomer(
            new Customer("Acme Corp", "billing@acme.com", "123 Business St")).getId();

        Map<String, Object> bill = resource.generateBill(customerId);

        assertThat(bill).containsKey("customer");
    }

    @Test
    void generateBillThrowsCustomerNotFoundForMissingCustomer() {
        assertThatThrownBy(() -> resource.generateBill(999L))
            .isInstanceOf(CustomerNotFoundException.class);
    }
}
