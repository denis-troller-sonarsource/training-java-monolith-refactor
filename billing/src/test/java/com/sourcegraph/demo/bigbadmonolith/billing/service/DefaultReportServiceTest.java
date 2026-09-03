package com.sourcegraph.demo.bigbadmonolith.billing.service;

import com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerBillLine;
import com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerBillReport;
import com.sourcegraph.demo.bigbadmonolith.billing.api.MonthlySummaryRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.ReportService;
import com.sourcegraph.demo.bigbadmonolith.billing.api.RevenueByCategoryRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.RevenueByCustomerRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.UserRevenueRow;
import com.sourcegraph.demo.bigbadmonolith.billing.repository.JdbcReportRepository;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.catalog.repository.JdbcBillingCategoryRepository;
import com.sourcegraph.demo.bigbadmonolith.catalog.service.DefaultBillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.repository.JdbcCustomerRepository;
import com.sourcegraph.demo.bigbadmonolith.customers.service.DefaultCustomerService;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourService;
import com.sourcegraph.demo.bigbadmonolith.timesheet.repository.JdbcBillableHourRepository;
import com.sourcegraph.demo.bigbadmonolith.timesheet.service.DefaultBillableHourService;
import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;
import com.sourcegraph.demo.bigbadmonolith.users.repository.JdbcUserRepository;
import com.sourcegraph.demo.bigbadmonolith.users.service.DefaultUserService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Characterization + regression tests for {@link ReportService} against a real in-memory Derby
 * database. Seeds data through the context services so the report SQL runs against real rows.
 *
 * <p>Includes the month-end fix: the legacy {@code reports.jsp} computed the month end as a naive
 * {@code year-month-31}, which excluded/threw for short months. The extracted service uses
 * {@link java.time.YearMonth#atEndOfMonth()} instead, and these tests lock that in.
 */
class DefaultReportServiceTest {

    private InMemoryDatabase db;
    private ReportService service;

    private BillingCategoryService categoryService;
    private BillableHourService billableHourService;
    private UserService userService;

    private Long customerId;
    private Long userId;
    private Long categoryId;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        service = new DefaultReportService(new JdbcReportRepository());

        categoryService = new DefaultBillingCategoryService(new JdbcBillingCategoryRepository());
        billableHourService = new DefaultBillableHourService(new JdbcBillableHourRepository());
        userService = new DefaultUserService(new JdbcUserRepository());

        Customer customer = new DefaultCustomerService(new JdbcCustomerRepository())
            .createCustomer(new Customer("Acme Corp", "billing@acme.test", "1 Road"));
        User user = userService.createUser(new User("user@example.com", "Sample User"));
        BillingCategory category = categoryService
            .createCategory(new BillingCategory("Development", "Dev work", new BigDecimal("100.00")));

        customerId = customer.getId();
        userId = user.getId();
        categoryId = category.getId();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    private BillableHour seedHour(BigDecimal hours, Long catId, LocalDate dateLogged) {
        return billableHourService.logHour(
            new BillableHour(customerId, userId, catId, hours, "note", dateLogged));
    }

    // ---- customerBill ------------------------------------------------------------------------

    @Test
    void customerBillReturnsCustomerContactAndTotals() {
        seedHour(new BigDecimal("2.00"), categoryId, LocalDate.of(2024, 1, 15));
        seedHour(new BigDecimal("3.50"), categoryId, LocalDate.of(2024, 1, 16));

        CustomerBillReport bill = service.customerBill(customerId);

        assertThat(bill.customerName()).isEqualTo("Acme Corp");
        assertThat(bill.customerEmail()).isEqualTo("billing@acme.test");
        // rate 100.00 * (2.00 + 3.50) = 550.00
        assertThat(bill.totalHours()).isEqualByComparingTo(new BigDecimal("5.50"));
        assertThat(bill.totalAmount()).isEqualByComparingTo(new BigDecimal("550.00"));
    }

    @Test
    void customerBillMapsLinesNewestFirstWithLineTotals() {
        seedHour(new BigDecimal("2.00"), categoryId, LocalDate.of(2024, 1, 15));
        seedHour(new BigDecimal("3.00"), categoryId, LocalDate.of(2024, 1, 20));

        CustomerBillReport bill = service.customerBill(customerId);

        // ORDER BY date_logged DESC -> the 2024-01-20 row comes first.
        assertThat(bill.lines()).hasSize(2);
        CustomerBillLine first = bill.lines().get(0);
        assertThat(first.dateLogged()).isEqualTo(LocalDate.of(2024, 1, 20));
        assertThat(first.userName()).isEqualTo("Sample User");
        assertThat(first.categoryName()).isEqualTo("Development");
        assertThat(first.hourlyRate()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(first.lineTotal()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void customerBillWithNoHoursHasZeroTotals() {
        CustomerBillReport bill = service.customerBill(customerId);

        assertThat(bill.customerName()).isEqualTo("Acme Corp");
        assertThat(bill.lines()).isEmpty();
        assertThat(bill.totalHours()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(bill.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---- monthlySummary: the month-end fix ---------------------------------------------------

    @Test
    void monthlySummaryIncludesRowOnTheRealLastDayOfAShortMonth() {
        // 2024-04-30 is the true last day of April (a 30-day month). The legacy
        // "year-month-31" end date would have been 2024-04-31, which is invalid and would have
        // thrown before this row could ever be included. The fix uses YearMonth.atEndOfMonth().
        seedHour(new BigDecimal("4.00"), categoryId, LocalDate.of(2024, 4, 30));
        seedHour(new BigDecimal("1.00"), categoryId, LocalDate.of(2024, 4, 1));

        List<MonthlySummaryRow> rows = service.monthlySummary(2024, 4);

        assertThat(rows).hasSize(1);
        MonthlySummaryRow row = rows.get(0);
        assertThat(row.customerName()).isEqualTo("Acme Corp");
        // Both the first-of-month and the last-of-month rows are counted: (4.00 + 1.00) hours.
        assertThat(row.totalHours()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(row.totalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void monthlySummaryDoesNotThrowForFebruary() {
        // Feb has no 31st (nor 30th, nor 29th in 2023). The legacy Date.valueOf("2023-02-31")
        // threw IllegalArgumentException; atEndOfMonth() yields the valid 2023-02-28.
        seedHour(new BigDecimal("2.00"), categoryId, LocalDate.of(2023, 2, 28));

        assertThatCode(() -> {
            List<MonthlySummaryRow> rows = service.monthlySummary(2023, 2);
            assertThat(rows).hasSize(1);
            assertThat(rows.get(0).totalHours()).isEqualByComparingTo(new BigDecimal("2.00"));
        }).doesNotThrowAnyException();
    }

    @Test
    void monthlySummaryExcludesRowsOutsideTheMonth() {
        seedHour(new BigDecimal("2.00"), categoryId, LocalDate.of(2024, 3, 15));
        seedHour(new BigDecimal("9.00"), categoryId, LocalDate.of(2024, 4, 1));
        seedHour(new BigDecimal("9.00"), categoryId, LocalDate.of(2024, 2, 29));

        List<MonthlySummaryRow> rows = service.monthlySummary(2024, 3);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).totalHours()).isEqualByComparingTo(new BigDecimal("2.00"));
    }

    // ---- revenueByCustomer / revenueByCategory ----------------------------------------------

    @Test
    void revenueByCustomerAggregatesHoursRevenueAndAverageRate() {
        BillingCategory consulting = categoryService
            .createCategory(new BillingCategory("Consulting", "Advisory", new BigDecimal("200.00")));
        seedHour(new BigDecimal("2.00"), categoryId, LocalDate.of(2024, 1, 5));
        seedHour(new BigDecimal("1.00"), consulting.getId(), LocalDate.of(2024, 1, 6));

        List<RevenueByCustomerRow> rows = service.revenueByCustomer();

        assertThat(rows).hasSize(1);
        RevenueByCustomerRow row = rows.get(0);
        assertThat(row.customerName()).isEqualTo("Acme Corp");
        assertThat(row.totalHours()).isEqualByComparingTo(new BigDecimal("3.00"));
        // 2.00*100 + 1.00*200 = 400.00
        assertThat(row.totalRevenue()).isEqualByComparingTo(new BigDecimal("400.00"));
        // AVG(hourly_rate) over the two rows = (100 + 200) / 2 = 150.
        assertThat(row.averageRate()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void revenueByCustomerReturnsZerosForCustomerWithNoHours() {
        // No hours seeded: the LEFT JOIN keeps the customer with NULL aggregates, surfaced as zero.
        List<RevenueByCustomerRow> rows = service.revenueByCustomer();

        assertThat(rows).hasSize(1);
        RevenueByCustomerRow row = rows.get(0);
        assertThat(row.customerName()).isEqualTo("Acme Corp");
        assertThat(row.totalHours()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(row.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(row.averageRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void revenueByCategoryUsesCoalesceZeroForCategoryWithNoHours() {
        categoryService.createCategory(new BillingCategory("Idle", "Never used", new BigDecimal("50.00")));
        seedHour(new BigDecimal("2.00"), categoryId, LocalDate.of(2024, 1, 5));

        List<RevenueByCategoryRow> rows = service.revenueByCategory();

        assertThat(rows).extracting(RevenueByCategoryRow::categoryName)
            .containsExactlyInAnyOrder("Development", "Idle");

        RevenueByCategoryRow development = rows.stream()
            .filter(r -> r.categoryName().equals("Development")).findFirst().orElseThrow();
        assertThat(development.totalHours()).isEqualByComparingTo(new BigDecimal("2.00"));
        assertThat(development.totalRevenue()).isEqualByComparingTo(new BigDecimal("200.00"));

        RevenueByCategoryRow idleRow = rows.stream()
            .filter(r -> r.categoryName().equals("Idle")).findFirst().orElseThrow();
        assertThat(idleRow.hourlyRate()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(idleRow.totalHours()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(idleRow.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---- totalRevenue (dashboard) -----------------------------------------------------------

    @Test
    void totalRevenueSumsHoursTimesRateAcrossAllHours() {
        BillingCategory consulting = categoryService
            .createCategory(new BillingCategory("Consulting", "Advisory", new BigDecimal("200.00")));
        seedHour(new BigDecimal("2.00"), categoryId, LocalDate.of(2024, 1, 5));
        seedHour(new BigDecimal("3.00"), consulting.getId(), LocalDate.of(2024, 1, 6));

        // 2.00*100 + 3.00*200 = 800.00
        assertThat(service.totalRevenue()).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    void totalRevenueIsZeroWhenNoHoursLogged() {
        assertThat(service.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ---- revenueByUser ----------------------------------------------------------------------

    @Test
    void revenueByUserAggregatesHoursAndRevenuePerUser() {
        BillingCategory consulting = categoryService
            .createCategory(new BillingCategory("Consulting", "Advisory", new BigDecimal("200.00")));
        seedHour(new BigDecimal("2.00"), categoryId, LocalDate.of(2024, 1, 5));
        seedHour(new BigDecimal("1.00"), consulting.getId(), LocalDate.of(2024, 1, 6));

        List<UserRevenueRow> rows = service.revenueByUser();

        UserRevenueRow row = rows.stream()
            .filter(r -> r.userId().equals(userId)).findFirst().orElseThrow();
        assertThat(row.userName()).isEqualTo("Sample User");
        assertThat(row.userEmail()).isEqualTo("user@example.com");
        assertThat(row.totalHours()).isEqualByComparingTo(new BigDecimal("3.00"));
        // 2.00*100 + 1.00*200 = 400.00
        assertThat(row.totalRevenue()).isEqualByComparingTo(new BigDecimal("400.00"));
    }

    @Test
    void revenueByUserReturnsZerosForUserWithNoHours() {
        // A second user with no billable hours: the LEFT JOIN keeps them with COALESCE(0) aggregates.
        User idle = userService.createUser(new User("idle@example.com", "Idle User"));
        seedHour(new BigDecimal("2.00"), categoryId, LocalDate.of(2024, 1, 5));

        List<UserRevenueRow> rows = service.revenueByUser();

        assertThat(rows).extracting(UserRevenueRow::userName)
            .containsExactlyInAnyOrder("Sample User", "Idle User");

        UserRevenueRow idleRow = rows.stream()
            .filter(r -> r.userId().equals(idle.getId())).findFirst().orElseThrow();
        assertThat(idleRow.totalHours()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(idleRow.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
