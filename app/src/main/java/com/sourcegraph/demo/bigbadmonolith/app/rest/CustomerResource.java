package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.util.List;

/**
 * REST resource for the customers context. Delegates entirely to {@link CustomerService} and never
 * touches the customers repository or service internals.
 */
@Path("customers")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    private final CustomerService customerService;

    @Inject
    public CustomerResource(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GET
    public List<Customer> list() {
        return customerService.listCustomers();
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") Long id) {
        Customer customer = customerService.getCustomer(id);
        if (customer == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(customer).build();
    }

    @POST
    public Response create(Customer customer) {
        Customer saved = customerService.createCustomer(customer);
        return Response.status(Status.CREATED).entity(saved).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") Long id, Customer customer) {
        customer.setId(id);
        boolean updated = customerService.updateCustomer(customer);
        if (!updated) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(customer).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = customerService.deleteCustomer(id);
        if (!deleted) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }
}
