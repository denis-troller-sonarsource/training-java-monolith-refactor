package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourService;

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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.util.List;

/**
 * REST resource for the timesheet context. Delegates entirely to {@link BillableHourService}.
 * The list endpoint accepts optional {@code customerId} / {@code userId} query filters.
 */
@Path("hours")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BillableHourResource {

    private final BillableHourService billableHourService;

    @Inject
    public BillableHourResource(BillableHourService billableHourService) {
        this.billableHourService = billableHourService;
    }

    @GET
    public List<BillableHour> list(@QueryParam("customerId") Long customerId,
                                   @QueryParam("userId") Long userId) {
        if (customerId != null) {
            return billableHourService.listHoursForCustomer(customerId);
        }
        if (userId != null) {
            return billableHourService.listHoursForUser(userId);
        }
        return billableHourService.listHours();
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") Long id) {
        BillableHour hour = billableHourService.getHour(id);
        if (hour == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(hour).build();
    }

    @POST
    public Response create(BillableHour hour) {
        BillableHour saved = billableHourService.logHour(hour);
        return Response.status(Status.CREATED).entity(saved).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") Long id, BillableHour hour) {
        hour.setId(id);
        boolean updated = billableHourService.updateHour(hour);
        if (!updated) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(hour).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = billableHourService.deleteHour(id);
        if (!deleted) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }
}
