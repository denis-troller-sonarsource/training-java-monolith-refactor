package com.sourcegraph.demo.bigbadmonolith.customers.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the {@link Customers} ServiceLoader bridge resolves the registered
 * {@link CustomerService} implementation (the temporary entry point for non-CDI callers).
 */
class CustomersFactoryTest {

    @Test
    void serviceResolvesRegisteredImplementation() {
        CustomerService service = Customers.service();

        assertThat(service).isNotNull();
    }
}
