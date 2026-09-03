package com.sourcegraph.demo.bigbadmonolith.app.rest;

import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryResourceTest extends RestResourceTestBase {

    private CategoryResource resource;

    @BeforeEach
    void createResource() {
        resource = new CategoryResource(categoryService);
    }

    private BillingCategory seedCategory() {
        return categoryService.createCategory(
            new BillingCategory("Development", "Software development work", new BigDecimal("150.00")));
    }

    @Test
    void listReturnsAllCategories() {
        seedCategory();

        assertThat(resource.list()).hasSize(1);
    }

    @Test
    void getReturnsCategoryWhenFound() {
        BillingCategory saved = seedCategory();

        Response response = resource.get(saved.getId());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(((BillingCategory) response.getEntity()).getName()).isEqualTo("Development");
    }

    @Test
    void getReturns404WhenMissing() {
        Response response = resource.get(999L);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void createReturns201WithSavedEntity() {
        Response response = resource.create(
            new BillingCategory("Consulting", "Business consulting", new BigDecimal("200.00")));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        assertThat(((BillingCategory) response.getEntity()).getId()).isNotNull();
    }

    @Test
    void updateReturnsOkWhenFound() {
        BillingCategory saved = seedCategory();

        Response response = resource.update(saved.getId(),
            new BillingCategory("Dev", "Updated", new BigDecimal("175.00")));

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(categoryService.getCategory(saved.getId()).getName()).isEqualTo("Dev");
    }

    @Test
    void updateReturns404WhenMissing() {
        Response response = resource.update(999L,
            new BillingCategory("X", "x", new BigDecimal("1.00")));

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteReturnsNoContentWhenFound() {
        BillingCategory saved = seedCategory();

        Response response = resource.delete(saved.getId());

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        assertThat(categoryService.listCategories()).isEmpty();
    }

    @Test
    void deleteReturns404WhenMissing() {
        Response response = resource.delete(999L);

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
