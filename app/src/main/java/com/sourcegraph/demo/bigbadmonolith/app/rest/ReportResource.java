package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerBillReport;
import com.sourcegraph.demo.bigbadmonolith.billing.api.MonthlySummaryRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.ReportService;
import com.sourcegraph.demo.bigbadmonolith.billing.api.RevenueByCategoryRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.RevenueByCustomerRow;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.util.List;

/**
 * REST resource exposing the billing reports. Delegates entirely to {@link ReportService}.
 */
@Path("reports")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class ReportResource {

    private final ReportService reportService;

    /** Required so Weld can create the normal-scoped client proxy; never used for real calls. */
    protected ReportResource() {
        this.reportService = null;
    }

    @Inject
    public ReportResource(ReportService reportService) {
        this.reportService = reportService;
    }

    @GET
    @Path("customer/{id}")
    public CustomerBillReport customerBill(@PathParam("id") Long id) {
        return reportService.customerBill(id);
    }

    @GET
    @Path("monthly")
    public Response monthly(@QueryParam("year") Integer year, @QueryParam("month") Integer month) {
        if (year == null || month == null) {
            return Response.status(Status.BAD_REQUEST)
                .entity("Both 'year' and 'month' query parameters are required")
                .build();
        }
        List<MonthlySummaryRow> summary = reportService.monthlySummary(year, month);
        return Response.ok(summary).build();
    }

    @GET
    @Path("revenue/by-customer")
    public List<RevenueByCustomerRow> revenueByCustomer() {
        return reportService.revenueByCustomer();
    }

    @GET
    @Path("revenue/by-category")
    public List<RevenueByCategoryRow> revenueByCategory() {
        return reportService.revenueByCategory();
    }
}
