package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.dao.BillableHourDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.BillingCategoryDAO;
import com.sourcegraph.demo.bigbadmonolith.dao.CustomerDAO;
import com.sourcegraph.demo.bigbadmonolith.common.LibertyConnectionManager;
import com.sourcegraph.demo.bigbadmonolith.dao.UserDAO;
import com.sourcegraph.demo.bigbadmonolith.entity.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.entity.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.entity.Customer;
import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for {@link BillingService} against a real in-memory Derby database.
 * The service instantiates its own DAOs, so tests seed data through the same DAOs. These lock in
 * the service's current billing, reporting, and validation behavior before the refactoring begins.
 */
class BillingServiceTest {

    private InMemoryDatabase db;
    private BillingService service;

    private BillingCategoryDAO categoryDAO;
    private BillableHourDAO billableHourDAO;

    private Long customerId;
    private Long userId;
    private Long categoryId;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        service = new BillingService();

        categoryDAO = new BillingCategoryDAO();
        billableHourDAO = new BillableHourDAO();

        Customer customer = new CustomerDAO().save(new Customer("Acme Corp", "billing@acme.test", "1 Road"));
        User user = new UserDAO().save(new User("user@example.com", "Sample User"));
        BillingCategory category = categoryDAO
            .save(new BillingCategory("Development", "Dev work", new BigDecimal("100.00")));

        customerId = customer.getId();
        userId = user.getId();
        categoryId = category.getId();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    private BillableHour seedHour(BigDecimal hours, Long catId, LocalDate dateLogged) throws SQLException {
        return billableHourDAO.save(
            new BillableHour(customerId, userId, catId, hours, "note", dateLogged));
    }

    private static final String DROP_BILLABLE_HOURS = "DROP TABLE billable_hours";

    private static final String CREATE_BILLABLE_HOURS_NO_CATEGORY_FK =
        "CREATE TABLE billable_hours ("
        + "id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 100, INCREMENT BY 1), "
        + "customer_id BIGINT NOT NULL, "
        + "user_id BIGINT NOT NULL, "
        + "category_id BIGINT NOT NULL, "
        + "hours DECIMAL(8,2) NOT NULL, "
        + "note VARCHAR(1000), "
        + "date_logged DATE NOT NULL, "
        + "created_at TIMESTAMP NOT NULL, "
        + "PRIMARY KEY (id), "
        + "FOREIGN KEY (customer_id) REFERENCES customers(id), "
        + "FOREIGN KEY (user_id) REFERENCES users(id))";

    /**
     * Recreates the empty {@code billable_hours} table without the {@code category_id} foreign key,
     * so an orphaned category reference (for which the category lookup returns null) can be
     * inserted through the DAO to characterize the service's null-category skip behavior. Must be
     * called before any billable hours are seeded.
     */
    private void removeCategoryForeignKey() throws SQLException {
        try (Connection conn = LibertyConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(DROP_BILLABLE_HOURS);
            stmt.executeUpdate(CREATE_BILLABLE_HOURS_NO_CATEGORY_FK);
        }
    }

    @Test
    void generateCustomerBillReturnsExpectedKeys() throws SQLException {
        seedHour(new BigDecimal("2.00"), categoryId, new LocalDate(2024, 1, 15));

        Map<String, Object> bill = service.generateCustomerBill(customerId);

        assertThat(bill).containsKeys(
            "customer", "billableHours", "totalHours", "totalAmount", "generatedDate");
    }

    @Test
    void generateCustomerBillSumsHoursAndAmount() throws SQLException {
        seedHour(new BigDecimal("2.00"), categoryId, new LocalDate(2024, 1, 15));
        seedHour(new BigDecimal("3.50"), categoryId, new LocalDate(2024, 1, 16));

        Map<String, Object> bill = service.generateCustomerBill(customerId);

        // rate 100.00 * (2.00 + 3.50) = 550.00
        assertThat((BigDecimal) bill.get("totalHours")).isEqualByComparingTo(new BigDecimal("5.50"));
        assertThat((BigDecimal) bill.get("totalAmount")).isEqualByComparingTo(new BigDecimal("550.00"));
        assertThat(bill.get("customer")).isInstanceOf(Customer.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateCustomerBillSkipsHoursWithNullCategory() throws SQLException {
        // Drop the category FK first so a row pointing at a non-existent category can be inserted.
        removeCategoryForeignKey();
        BillableHour counted = seedHour(new BigDecimal("2.00"), categoryId, new LocalDate(2024, 1, 15));
        // This second hour references a category id that will not resolve (lookup returns null).
        seedHour(new BigDecimal("9.00"), 9999L, new LocalDate(2024, 1, 16));

        Map<String, Object> bill = service.generateCustomerBill(customerId);

        // Only the hour with a resolvable category contributes to totals.
        assertThat((BigDecimal) bill.get("totalHours")).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat((BigDecimal) bill.get("totalAmount")).isEqualByComparingTo(new BigDecimal("200.00"));
        // But the raw billableHours list still contains every row for the customer.
        List<BillableHour> billableHours = (List<BillableHour>) bill.get("billableHours");
        assertThat(billableHours).extracting(BillableHour::getId).contains(counted.getId());
        assertThat(billableHours).hasSize(2);
    }

    @Test
    void generateCustomerBillThrowsWhenCustomerNotFound() {
        assertThatThrownBy(() -> service.generateCustomerBill(9999L))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateMonthlyReportIncludesOnlyInMonthRows() throws SQLException {
        BillingCategory consulting = categoryDAO
            .save(new BillingCategory("Consulting", "Advisory", new BigDecimal("200.00")));
        // In target month 2024-03.
        seedHour(new BigDecimal("2.00"), categoryId, new LocalDate(2024, 3, 5));
        seedHour(new BigDecimal("1.00"), consulting.getId(), new LocalDate(2024, 3, 20));
        // Out of target month.
        seedHour(new BigDecimal("10.00"), categoryId, new LocalDate(2024, 2, 5));
        seedHour(new BigDecimal("10.00"), categoryId, new LocalDate(2023, 3, 5));

        Map<String, Object> report = service.generateMonthlyReport(2024, 3);

        // 2.00*100 + 1.00*200 = 400.00 ; hours 3.00
        assertThat((BigDecimal) report.get("totalRevenue")).isEqualByComparingTo(new BigDecimal("400.00"));
        assertThat((BigDecimal) report.get("totalHours")).isEqualByComparingTo(new BigDecimal("3.00"));

        Map<String, BigDecimal> revenueByCategory = (Map<String, BigDecimal>) report.get("revenueByCategory");
        assertThat(revenueByCategory).containsOnlyKeys("Development", "Consulting");
        assertThat(revenueByCategory.get("Development")).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(revenueByCategory.get("Consulting")).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    void validateBillableHourReturnsEmptyForValidWeekdayEntry() {
        BillableHour hour = new BillableHour(
            customerId, userId, categoryId, new BigDecimal("5.00"), "note", new LocalDate(2024, 1, 15));

        String result = service.validateBillableHour(hour);

        assertThat(result).isEmpty();
    }

    @Test
    void validateBillableHourReportsInvalidCustomer() {
        BillableHour hour = new BillableHour(
            9999L, userId, categoryId, new BigDecimal("5.00"), "note", new LocalDate(2024, 1, 15));

        String result = service.validateBillableHour(hour);

        assertThat(result).contains("Invalid customer ID");
    }

    @Test
    void validateBillableHourReportsInvalidCategory() {
        BillableHour hour = new BillableHour(
            customerId, userId, 9999L, new BigDecimal("5.00"), "note", new LocalDate(2024, 1, 15));

        String result = service.validateBillableHour(hour);

        assertThat(result).contains("Invalid category ID");
    }

    @Test
    void validateBillableHourReportsNonPositiveHours() {
        BillableHour hour = new BillableHour(
            customerId, userId, categoryId, BigDecimal.ZERO, "note", new LocalDate(2024, 1, 15));

        String result = service.validateBillableHour(hour);

        assertThat(result).contains("Hours must be greater than zero");
    }

    @Test
    void validateBillableHourReportsMissingDate() {
        BillableHour hour = new BillableHour(
            customerId, userId, categoryId, new BigDecimal("5.00"), "note", null);

        String result = service.validateBillableHour(hour);

        assertThat(result).contains("Date logged is required");
    }

    @Test
    void validateBillableHourReportsFutureDate() {
        LocalDate future = LocalDate.now().plusDays(5);
        BillableHour hour = new BillableHour(
            customerId, userId, categoryId, new BigDecimal("5.00"), "note", future);

        String result = service.validateBillableHour(hour);

        assertThat(result).contains("cannot be in the future");
    }

    @Test
    void validateBillableHourWarnsOnWeekend() {
        // 2024-01-06 is a Saturday and is in the past.
        BillableHour hour = new BillableHour(
            customerId, userId, categoryId, new BigDecimal("5.00"), "note", new LocalDate(2024, 1, 6));

        String result = service.validateBillableHour(hour);

        assertThat(result).contains("Warning: Hours logged on weekend");
    }
}
