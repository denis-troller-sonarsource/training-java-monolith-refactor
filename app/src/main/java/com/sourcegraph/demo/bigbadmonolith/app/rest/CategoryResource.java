package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;

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
 * REST resource for the catalog context. Delegates entirely to {@link BillingCategoryService}.
 */
@Path("categories")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    private final BillingCategoryService categoryService;

    /** Required so Weld can create the normal-scoped client proxy; never used for real calls. */
    protected CategoryResource() {
        this.categoryService = null;
    }

    @Inject
    public CategoryResource(BillingCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GET
    public List<BillingCategory> list() {
        return categoryService.listCategories();
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") Long id) {
        BillingCategory category = categoryService.getCategory(id);
        if (category == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(category).build();
    }

    @POST
    public Response create(BillingCategory category) {
        BillingCategory saved = categoryService.createCategory(category);
        return Response.status(Status.CREATED).entity(saved).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") Long id, BillingCategory category) {
        category.setId(id);
        boolean updated = categoryService.updateCategory(category);
        if (!updated) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(category).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = categoryService.deleteCategory(id);
        if (!deleted) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }
}
