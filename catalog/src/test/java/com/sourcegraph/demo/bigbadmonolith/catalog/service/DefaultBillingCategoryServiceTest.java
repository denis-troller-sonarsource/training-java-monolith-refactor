package com.sourcegraph.demo.bigbadmonolith.catalog.service;

import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link DefaultBillingCategoryService} end to end against the JDBC repository and in-memory
 * Derby, covering the service delegation for every operation.
 */
class DefaultBillingCategoryServiceTest {

    private InMemoryDatabase db;
    private BillingCategoryService service;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        service = new DefaultBillingCategoryService();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void createCategoryAssignsId() {
        BillingCategory saved = service.createCategory(
            new BillingCategory("Development", "Dev work", new BigDecimal("150.00")));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void getCategoryReturnsCreatedCategory() {
        BillingCategory saved = service.createCategory(
            new BillingCategory("Development", "Dev work", new BigDecimal("150.00")));

        assertThat(service.getCategory(saved.getId()).getName()).isEqualTo("Development");
    }

    @Test
    void listCategoriesReturnsAll() {
        service.createCategory(new BillingCategory("Development", "Dev work", new BigDecimal("150.00")));
        service.createCategory(new BillingCategory("Consulting", "Advisory", new BigDecimal("200.00")));

        List<BillingCategory> all = service.listCategories();

        assertThat(all).extracting(BillingCategory::getName)
            .containsExactlyInAnyOrder("Development", "Consulting");
    }

    @Test
    void updateCategoryChangesRate() {
        BillingCategory saved = service.createCategory(
            new BillingCategory("Development", "Dev work", new BigDecimal("150.00")));
        saved.setHourlyRate(new BigDecimal("175.00"));

        boolean updated = service.updateCategory(saved);

        assertThat(updated).isTrue();
        assertThat(service.getCategory(saved.getId()).getHourlyRate())
            .isEqualByComparingTo(new BigDecimal("175.00"));
    }

    @Test
    void deleteCategoryRemovesCategory() {
        BillingCategory saved = service.createCategory(
            new BillingCategory("Development", "Dev work", new BigDecimal("150.00")));

        boolean deleted = service.deleteCategory(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(service.getCategory(saved.getId())).isNull();
    }
}
