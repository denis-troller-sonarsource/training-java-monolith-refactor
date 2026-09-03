package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerNotFoundException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps {@link CustomerNotFoundException} raised by the billing context to an HTTP 404 response,
 * so resource methods can let it propagate instead of throwing raw exceptions.
 */
@Provider
public class CustomerNotFoundExceptionMapper implements ExceptionMapper<CustomerNotFoundException> {

    @Override
    public Response toResponse(CustomerNotFoundException exception) {
        return Response.status(Status.NOT_FOUND)
            .entity(exception.getMessage())
            .type(MediaType.TEXT_PLAIN)
            .build();
    }
}
