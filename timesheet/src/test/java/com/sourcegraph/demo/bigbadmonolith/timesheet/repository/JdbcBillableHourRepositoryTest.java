package com.sourcegraph.demo.bigbadmonolith.timesheet.repository;

import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.Catalog;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customers;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import com.sourcegraph.demo.bigbadmonolith.users.api.Users;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization tests for {@link JdbcBillableHourRepository} against a real in-memory Derby
 * database. Foreign keys are enforced, so each test first seeds a customer, user, and billing
 * category via the sibling contexts' services and reuses their generated ids. These lock in the
 * repository's current CRUD behavior (including {@code ORDER BY date_logged DESC}).
 */
class JdbcBillableHourRepositoryTest {

    private InMemoryDatabase db;
    private JdbcBillableHourRepository dao;

    private Long customerId;
    private Long userId;
    private Long categoryId;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        dao = new JdbcBillableHourRepository();

        Customer customer = Customers.service().createCustomer(new Customer("Acme Corp", "billing@acme.test", "1 Road"));
        User user = Users.service().createUser(new User("user@example.com", "Sample User"));
        BillingCategory category = Catalog.service()
            .createCategory(new BillingCategory("Development", "Dev work", new BigDecimal("150.00")));

        customerId = customer.getId();
        userId = user.getId();
        categoryId = category.getId();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    private BillableHour newHour(BigDecimal hours, String note, LocalDate dateLogged) {
        return new BillableHour(customerId, userId, categoryId, hours, note, dateLogged);
    }

    @Test
    void savePopulatesGeneratedId() {
        BillableHour saved = dao.save(
            newHour(new BigDecimal("8.50"), "Work done", LocalDate.of(2024, 1, 15)));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findByIdReturnsSavedHour() {
        BillableHour saved = dao.save(
            newHour(new BigDecimal("8.50"), "Work done", LocalDate.of(2024, 1, 15)));

        BillableHour found = dao.findById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getCustomerId()).isEqualTo(customerId);
        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getCategoryId()).isEqualTo(categoryId);
        assertThat(found.getHours()).isEqualByComparingTo(new BigDecimal("8.50"));
        assertThat(found.getNote()).isEqualTo("Work done");
        assertThat(found.getDateLogged()).hasToString("2024-01-15");
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void findByIdReturnsNullWhenAbsent() {
        assertThat(dao.findById(9999L)).isNull();
    }

    @Test
    void findByCustomerIdOrdersByDateLoggedDescending() {
        dao.save(newHour(new BigDecimal("1.00"), "older", LocalDate.of(2024, 1, 1)));
        dao.save(newHour(new BigDecimal("2.00"), "newer", LocalDate.of(2024, 3, 1)));
        dao.save(newHour(new BigDecimal("3.00"), "middle", LocalDate.of(2024, 2, 1)));

        List<BillableHour> hours = dao.findByCustomerId(customerId);

        assertThat(hours)
            .extracting(BillableHour::getNote)
            .containsExactly("newer", "middle", "older");
    }

    @Test
    void findByCustomerIdReturnsEmptyForUnknownCustomer() {
        dao.save(newHour(new BigDecimal("1.00"), "work", LocalDate.of(2024, 1, 1)));

        assertThat(dao.findByCustomerId(9999L)).isEmpty();
    }

    @Test
    void findByUserIdReturnsHoursForUser() {
        dao.save(newHour(new BigDecimal("1.00"), "first", LocalDate.of(2024, 1, 1)));
        dao.save(newHour(new BigDecimal("2.00"), "second", LocalDate.of(2024, 2, 1)));

        List<BillableHour> hours = dao.findByUserId(userId);

        assertThat(hours)
            .extracting(BillableHour::getNote)
            .containsExactly("second", "first");
    }

    @Test
    void findAllReturnsEveryHour() {
        dao.save(newHour(new BigDecimal("1.00"), "a", LocalDate.of(2024, 1, 1)));
        dao.save(newHour(new BigDecimal("2.00"), "b", LocalDate.of(2024, 2, 1)));

        List<BillableHour> all = dao.findAll();

        assertThat(all)
            .extracting(BillableHour::getNote)
            .containsExactly("b", "a");
    }

    @Test
    void updateChangesPersistedFields() {
        BillableHour saved = dao.save(
            newHour(new BigDecimal("8.50"), "Work done", LocalDate.of(2024, 1, 15)));
        saved.setHours(new BigDecimal("4.25"));
        saved.setNote("Updated note");
        saved.setDateLogged(LocalDate.of(2024, 2, 20));

        boolean updated = dao.update(saved);

        assertThat(updated).isTrue();
        BillableHour reloaded = dao.findById(saved.getId());
        assertThat(reloaded.getHours()).isEqualByComparingTo(new BigDecimal("4.25"));
        assertThat(reloaded.getNote()).isEqualTo("Updated note");
        assertThat(reloaded.getDateLogged()).hasToString("2024-02-20");
    }

    @Test
    void deleteRemovesHour() {
        BillableHour saved = dao.save(
            newHour(new BigDecimal("8.50"), "Work done", LocalDate.of(2024, 1, 15)));

        boolean deleted = dao.delete(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(dao.findById(saved.getId())).isNull();
    }
}
