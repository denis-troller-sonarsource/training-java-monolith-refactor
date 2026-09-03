package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.billing.api.BillingService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

/**
 * REST resource for generating customer bills. Delegates to {@link BillingService}; a missing
 * customer surfaces as {@link com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerNotFoundException},
 * mapped to 404 by {@link CustomerNotFoundExceptionMapper}.
 */
@Path("bills")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class BillResource {

    private final BillingService billingService;

    /** Required so Weld can create the normal-scoped client proxy; never used for real calls. */
    protected BillResource() {
        this.billingService = null;
    }

    @Inject
    public BillResource(BillingService billingService) {
        this.billingService = billingService;
    }

    @GET
    @Path("{customerId}")
    public Map<String, Object> generateBill(@PathParam("customerId") Long customerId) {
        return billingService.generateCustomerBill(customerId);
    }
}
