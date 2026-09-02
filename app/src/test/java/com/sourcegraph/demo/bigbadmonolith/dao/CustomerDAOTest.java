package com.sourcegraph.demo.bigbadmonolith.dao;

import com.sourcegraph.demo.bigbadmonolith.entity.Customer;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for {@link CustomerDAO} against a real in-memory Derby database.
 * These lock in the DAO's current CRUD behavior before the modular refactoring begins.
 */
class CustomerDAOTest {

    private InMemoryDatabase db;
    private CustomerDAO dao;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        dao = new CustomerDAO();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void savePopulatesGeneratedId() throws SQLException {
        Customer saved = dao.save(new Customer("Acme Corp", "billing@acme.test", "1 Road"));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findByIdReturnsSavedCustomer() throws SQLException {
        Customer saved = dao.save(new Customer("Acme Corp", "billing@acme.test", "1 Road"));

        Customer found = dao.findById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Acme Corp");
        assertThat(found.getEmail()).isEqualTo("billing@acme.test");
        assertThat(found.getAddress()).isEqualTo("1 Road");
    }

    @Test
    void findByIdReturnsNullWhenAbsent() throws SQLException {
        assertThat(dao.findById(9999L)).isNull();
    }

    @Test
    void findAllOrdersByCreatedAtDescending() throws SQLException {
        Customer older = new Customer("Older", "older@test", "addr");
        older.setCreatedAt(new DateTime(2020, 1, 1, 0, 0));
        Customer newer = new Customer("Newer", "newer@test", "addr");
        newer.setCreatedAt(new DateTime(2024, 1, 1, 0, 0));
        dao.save(older);
        dao.save(newer);

        List<Customer> all = dao.findAll();

        assertThat(all).extracting(Customer::getName).containsExactly("Newer", "Older");
    }

    @Test
    void updateChangesPersistedFields() throws SQLException {
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
    void deleteRemovesCustomer() throws SQLException {
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
}
