package com.sourcegraph.demo.bigbadmonolith.customers.api;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic accessor coverage for the {@link Customer} model, including the no-arg constructor used by
 * frameworks that populate via setters.
 */
class CustomerTest {

    @Test
    void noArgConstructorWithSettersPopulatesFields() {
        Instant created = Instant.parse("2024-01-01T00:00:00Z");
        Customer customer = new Customer();
        customer.setId(7L);
        customer.setName("Acme Corp");
        customer.setEmail("billing@acme.test");
        customer.setAddress("1 Road");
        customer.setCreatedAt(created);

        assertThat(customer.getId()).isEqualTo(7L);
        assertThat(customer.getName()).isEqualTo("Acme Corp");
        assertThat(customer.getEmail()).isEqualTo("billing@acme.test");
        assertThat(customer.getAddress()).isEqualTo("1 Road");
        assertThat(customer.getCreatedAt()).isEqualTo(created);
    }
}
