package com.sourcegraph.demo.bigbadmonolith.dao;

import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for {@link BillingCategoryDAO} against a real in-memory Derby database.
 * These lock in the DAO's current CRUD behavior before the modular refactoring begins.
 */
class BillingCategoryDAOTest {

    private InMemoryDatabase db;
    private BillingCategoryDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        dao = new BillingCategoryDAO();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void savePopulatesGeneratedId() throws SQLException {
        BillingCategory saved = dao.save(
            new BillingCategory("Development", "Dev work", new BigDecimal("150.00")));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void saveRejectsNullCategory() {
        assertThatThrownBy(() -> dao.save(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findByIdReturnsSavedCategory() throws SQLException {
        BillingCategory saved = dao.save(
            new BillingCategory("Consulting", "Advisory", new BigDecimal("200.50")));

        BillingCategory found = dao.findById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Consulting");
        assertThat(found.getDescription()).isEqualTo("Advisory");
        assertThat(found.getHourlyRate()).isEqualByComparingTo(new BigDecimal("200.50"));
    }

    @Test
    void findByIdReturnsNullWhenAbsent() throws SQLException {
        assertThat(dao.findById(9999L)).isNull();
    }

    @Test
    void findAllOrdersByNameAscending() throws SQLException {
        dao.save(new BillingCategory("Support", "Support work", new BigDecimal("100.00")));
        dao.save(new BillingCategory("Consulting", "Advisory", new BigDecimal("200.00")));
        dao.save(new BillingCategory("Development", "Dev work", new BigDecimal("150.00")));

        List<BillingCategory> all = dao.findAll();

        assertThat(all)
            .extracting(BillingCategory::getName)
            .containsExactly("Consulting", "Development", "Support");
    }

    @Test
    void updateChangesPersistedFields() throws SQLException {
        BillingCategory saved = dao.save(
            new BillingCategory("Development", "Dev work", new BigDecimal("150.00")));
        saved.setName("Engineering");
        saved.setDescription("Engineering work");
        saved.setHourlyRate(new BigDecimal("175.00"));

        boolean updated = dao.update(saved);

        assertThat(updated).isTrue();
        BillingCategory reloaded = dao.findById(saved.getId());
        assertThat(reloaded.getName()).isEqualTo("Engineering");
        assertThat(reloaded.getDescription()).isEqualTo("Engineering work");
        assertThat(reloaded.getHourlyRate()).isEqualByComparingTo(new BigDecimal("175.00"));
    }

    @Test
    void deleteRemovesCategory() throws SQLException {
        BillingCategory saved = dao.save(
            new BillingCategory("Development", "Dev work", new BigDecimal("150.00")));

        boolean deleted = dao.delete(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(dao.findById(saved.getId())).isNull();
    }
}
