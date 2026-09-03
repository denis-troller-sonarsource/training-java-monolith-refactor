package com.sourcegraph.demo.bigbadmonolith.customers.repository;

import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for {@link JdbcCustomerRepository} against a real in-memory Derby database.
 * These lock in the repository's current CRUD behavior, including the validation contract, before
 * the modular refactoring is finalized.
 */
class JdbcCustomerRepositoryTest {

    private InMemoryDatabase db;
    private JdbcCustomerRepository dao;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        dao = new JdbcCustomerRepository();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void savePopulatesGeneratedId() {
        Customer saved = dao.save(new Customer("Acme Corp", "billing@acme.test", "1 Road"));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findByIdReturnsSavedCustomer() {
        Customer saved = dao.save(new Customer("Acme Corp", "billing@acme.test", "1 Road"));

        Customer found = dao.findById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Acme Corp");
        assertThat(found.getEmail()).isEqualTo("billing@acme.test");
        assertThat(found.getAddress()).isEqualTo("1 Road");
    }

    @Test
    void findByIdReturnsNullWhenAbsent() {
        assertThat(dao.findById(9999L)).isNull();
    }

    @Test
    void findAllOrdersByCreatedAtDescending() {
        Customer older = new Customer("Older", "older@test", "addr");
        older.setCreatedAt(Instant.parse("2020-01-01T00:00:00Z"));
        Customer newer = new Customer("Newer", "newer@test", "addr");
        newer.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
        dao.save(older);
        dao.save(newer);

        List<Customer> all = dao.findAll();

        assertThat(all).extracting(Customer::getName).containsExactly("Newer", "Older");
    }

    @Test
    void updateChangesPersistedFields() {
        Customer saved = dao.save(new Customer("Acme Corp", "billing@acme.test", "1 Road"));
        saved.setName("Acme Renamed");
        saved.setAddress("2 Avenue");

        boolean updated = dao.update(saved);

        assertThat(updated).isTrue();
        Customer reloaded = dao.findById(saved.getId());
        assertThat(reloaded.getName()).isEqualTo("Acme Renamed");
        assertThat(reloaded.getAddress()).isEqualTo("2 Avenue");
    }

    @Test
    void deleteRemovesCustomer() {
        Customer saved = dao.save(new Customer("Acme Corp", "billing@acme.test", "1 Road"));

        boolean deleted = dao.delete(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(dao.findById(saved.getId())).isNull();
    }

    @Test
    void saveRejectsNullCustomer() {
        assertThatThrownBy(() -> dao.save(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveRejectsBlankName() {
        Customer blankName = new Customer("  ", "e@test", "addr");

        assertThatThrownBy(() -> dao.save(blankName))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void saveRejectsBlankEmail() {
        Customer blankEmail = new Customer("Acme", "  ", "addr");

        assertThatThrownBy(() -> dao.save(blankEmail))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsNullCustomer() {
        assertThatThrownBy(() -> dao.update(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsNullId() {
        Customer noId = new Customer("Acme", "e@test", "addr");

        assertThatThrownBy(() -> dao.update(noId))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsBlankName() {
        Customer badName = new Customer(1L, "  ", "e@test", "addr", Instant.now().truncatedTo(ChronoUnit.MILLIS));

        assertThatThrownBy(() -> dao.update(badName))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsBlankEmail() {
        Customer badEmail = new Customer(1L, "Acme", "  ", "addr", Instant.now().truncatedTo(ChronoUnit.MILLIS));

        assertThatThrownBy(() -> dao.update(badEmail))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
