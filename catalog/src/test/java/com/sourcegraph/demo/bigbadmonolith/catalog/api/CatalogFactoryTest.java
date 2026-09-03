package com.sourcegraph.demo.bigbadmonolith.catalog.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the {@link Catalog} ServiceLoader bridge resolves the registered
 * {@link BillingCategoryService} implementation (the temporary entry point for non-CDI callers).
 */
class CatalogFactoryTest {

    @Test
    void serviceResolvesRegisteredImplementation() {
        BillingCategoryService service = Catalog.service();

        assertThat(service).isNotNull();
    }
}
