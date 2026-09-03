package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerNotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerNotFoundExceptionMapperTest {

    @Test
    void mapsExceptionToNotFoundResponse() {
        CustomerNotFoundExceptionMapper mapper = new CustomerNotFoundExceptionMapper();

        Response response = mapper.toResponse(new CustomerNotFoundException(42L));

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        assertThat(response.getEntity().toString()).contains("42");
    }
}
