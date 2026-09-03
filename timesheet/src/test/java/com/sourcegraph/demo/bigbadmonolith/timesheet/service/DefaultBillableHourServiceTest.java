package com.sourcegraph.demo.bigbadmonolith.timesheet.service;

import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.repository.JdbcBillingCategoryRepository;
import com.sourcegraph.demo.bigbadmonolith.catalog.service.DefaultBillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.repository.JdbcCustomerRepository;
import com.sourcegraph.demo.bigbadmonolith.customers.service.DefaultCustomerService;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourService;
import com.sourcegraph.demo.bigbadmonolith.timesheet.repository.JdbcBillableHourRepository;
import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import com.sourcegraph.demo.bigbadmonolith.users.repository.JdbcUserRepository;
import com.sourcegraph.demo.bigbadmonolith.users.service.DefaultUserService;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link DefaultBillableHourService} end to end against the JDBC repository and in-memory
 * Derby, covering the service delegation for every operation. Foreign keys are enforced, so a
 * customer, user, and billing category are seeded via the sibling contexts' services first.
 */
class DefaultBillableHourServiceTest {

    private InMemoryDatabase db;
    private BillableHourService service;

    private Long customerId;
    private Long userId;
    private Long categoryId;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        service = new DefaultBillableHourService(new JdbcBillableHourRepository());

        Customer customer = new DefaultCustomerService(new JdbcCustomerRepository())
            .createCustomer(new Customer("Acme Corp", "billing@acme.test", "1 Road"));
        User user = new DefaultUserService(new JdbcUserRepository())
            .createUser(new User("user@example.com", "Sample User"));
        BillingCategory category = new DefaultBillingCategoryService(new JdbcBillingCategoryRepository())
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
    void logHourAssignsId() {
        BillableHour saved = service.logHour(
            newHour(new BigDecimal("8.50"), "Work done", LocalDate.of(2024, 1, 15)));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void getHourReturnsLoggedHour() {
        BillableHour saved = service.logHour(
            newHour(new BigDecimal("8.50"), "Work done", LocalDate.of(2024, 1, 15)));

        assertThat(service.getHour(saved.getId()).getNote()).isEqualTo("Work done");
    }

    @Test
    void listHoursForCustomerReturnsCustomerHours() {
        service.logHour(newHour(new BigDecimal("1.00"), "older", LocalDate.of(2024, 1, 1)));
        service.logHour(newHour(new BigDecimal("2.00"), "newer", LocalDate.of(2024, 3, 1)));

        List<BillableHour> hours = service.listHoursForCustomer(customerId);

        assertThat(hours)
            .extracting(BillableHour::getNote)
            .containsExactly("newer", "older");
    }

    @Test
    void listHoursForUserReturnsUserHours() {
        service.logHour(newHour(new BigDecimal("1.00"), "first", LocalDate.of(2024, 1, 1)));
        service.logHour(newHour(new BigDecimal("2.00"), "second", LocalDate.of(2024, 2, 1)));

        List<BillableHour> hours = service.listHoursForUser(userId);

        assertThat(hours)
            .extracting(BillableHour::getNote)
            .containsExactly("second", "first");
    }

    @Test
    void listHoursReturnsAll() {
        service.logHour(newHour(new BigDecimal("1.00"), "a", LocalDate.of(2024, 1, 1)));
        service.logHour(newHour(new BigDecimal("2.00"), "b", LocalDate.of(2024, 2, 1)));

        List<BillableHour> all = service.listHours();

        assertThat(all).hasSize(2);
    }

    @Test
    void updateHourChangesPersistedFields() {
        BillableHour saved = service.logHour(
            newHour(new BigDecimal("8.50"), "Work done", LocalDate.of(2024, 1, 15)));
        saved.setHours(new BigDecimal("4.25"));
        saved.setNote("Updated note");

        boolean updated = service.updateHour(saved);

        assertThat(updated).isTrue();
        assertThat(service.getHour(saved.getId()).getHours()).isEqualByComparingTo(new BigDecimal("4.25"));
        assertThat(service.getHour(saved.getId()).getNote()).isEqualTo("Updated note");
    }

    @Test
    void deleteHourRemovesHour() {
        BillableHour saved = service.logHour(
            newHour(new BigDecimal("8.50"), "Work done", LocalDate.of(2024, 1, 15)));

        boolean deleted = service.deleteHour(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(service.getHour(saved.getId())).isNull();
    }
}
