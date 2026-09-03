package com.sourcegraph.demo.bigbadmonolith.app.rest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Each JAX-RS resource is {@code @ApplicationScoped}, so Weld needs a non-private no-arg constructor
 * to build the client proxy (WELD-001435 otherwise). Those constructors are only invoked by the CDI
 * container at runtime; this test simply exercises them so the proxy seam stays present and covered.
 */
class RestResourceProxyConstructorTest {

    @Test
    void allResourcesHaveTheProxyConstructor() {
        assertThat(new CustomerResource()).isNotNull();
        assertThat(new UserResource()).isNotNull();
        assertThat(new CategoryResource()).isNotNull();
        assertThat(new BillableHourResource()).isNotNull();
        assertThat(new ReportResource()).isNotNull();
        assertThat(new BillResource()).isNotNull();
    }
}
