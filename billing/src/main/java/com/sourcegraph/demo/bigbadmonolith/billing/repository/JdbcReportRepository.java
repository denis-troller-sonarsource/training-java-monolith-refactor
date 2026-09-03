package com.sourcegraph.demo.bigbadmonolith.billing.repository;

import com.sourcegraph.demo.bigbadmonolith.billing.api.CustomerBillLine;
import com.sourcegraph.demo.bigbadmonolith.billing.api.MonthlySummaryRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.RevenueByCategoryRow;
import com.sourcegraph.demo.bigbadmonolith.billing.api.RevenueByCustomerRow;
import com.sourcegraph.demo.bigbadmonolith.common.DataAccessException;
import com.sourcegraph.demo.bigbadmonolith.common.JdbcSupport;

import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * JDBC-backed data access for the billing reports. Runs the report SQL through {@link JdbcSupport}
 * (which sources connections from {@code LibertyConnectionManager} and wraps {@link SQLException} in
 * {@link DataAccessException}). Replaces the raw {@code DriverManager}/hardcoded-URL/{@code Statement}
 * JDBC that previously lived in {@code reports.jsp}.
 */
@ApplicationScoped
public class JdbcReportRepository {

    private static final String COL_NAME = "name";
    private static final String COL_TOTAL_HOURS = "total_hours";
    private static final String COL_TOTAL_REVENUE = "total_revenue";
    private static final String COL_HOURLY_RATE = "hourly_rate";

    private static final String CUSTOMER_NAME_EMAIL_SQL =
        "SELECT name, email FROM customers WHERE id = ?";

    private static final String CUSTOMER_BILL_LINES_SQL =
        "SELECT bh.date_logged, u.name as user_name, bc.name as category_name, "
        + "bh.hours, bc.hourly_rate, bh.hours * bc.hourly_rate as line_total, bh.note "
        + "FROM billable_hours bh "
        + "JOIN users u ON bh.user_id = u.id "
        + "JOIN billing_categories bc ON bh.category_id = bc.id "
        + "WHERE bh.customer_id = ? "
        + "ORDER BY bh.date_logged DESC";

    private static final String MONTHLY_SUMMARY_SQL =
        "SELECT c.name as customer_name, "
        + "SUM(bh.hours) as total_hours, "
        + "SUM(bh.hours * bc.hourly_rate) as total_amount "
        + "FROM billable_hours bh "
        + "JOIN customers c ON bh.customer_id = c.id "
        + "JOIN billing_categories bc ON bh.category_id = bc.id "
        + "WHERE bh.date_logged >= ? AND bh.date_logged <= ? "
        + "GROUP BY c.name "
        + "ORDER BY total_amount DESC";

    private static final String REVENUE_BY_CUSTOMER_SQL =
        "SELECT c.name, "
        + "SUM(bh.hours) as total_hours, "
        + "SUM(bh.hours * bc.hourly_rate) as total_revenue, "
        + "AVG(bc.hourly_rate) as avg_rate "
        + "FROM customers c "
        + "LEFT JOIN billable_hours bh ON c.id = bh.customer_id "
        + "LEFT JOIN billing_categories bc ON bh.category_id = bc.id "
        + "GROUP BY c.name "
        + "ORDER BY total_revenue DESC";

    private static final String REVENUE_BY_CATEGORY_SQL =
        "SELECT bc.name, bc.hourly_rate, "
        + "COALESCE(SUM(bh.hours), 0) as total_hours, "
        + "COALESCE(SUM(bh.hours * bc.hourly_rate), 0) as total_revenue "
        + "FROM billing_categories bc "
        + "LEFT JOIN billable_hours bh ON bc.id = bh.category_id "
        + "GROUP BY bc.name, bc.hourly_rate "
        + "ORDER BY total_revenue DESC";

    /** A customer's display name and email, or {@code null} when the customer does not exist. */
    public record CustomerContact(String name, String email) {
    }

    /** Looks up a customer's name and email by id, or returns {@code null} when not found. */
    public CustomerContact findCustomerContact(Long customerId) {
        return JdbcSupport.queryOne(CUSTOMER_NAME_EMAIL_SQL, "Failed to load customer for bill",
            stmt -> stmt.setLong(1, customerId),
            rs -> new CustomerContact(rs.getString(COL_NAME), rs.getString("email")));
    }

    /** Returns the billable-hour lines for a customer's bill, newest first. */
    public List<CustomerBillLine> findBillLines(Long customerId) {
        return JdbcSupport.queryList(CUSTOMER_BILL_LINES_SQL, "Failed to load customer bill lines",
            stmt -> stmt.setLong(1, customerId),
            JdbcReportRepository::mapBillLine);
    }

    /** Returns per-customer monthly totals for the inclusive date range [startDate, endDate]. */
    public List<MonthlySummaryRow> monthlySummary(LocalDate startDate, LocalDate endDate) {
        return JdbcSupport.queryList(MONTHLY_SUMMARY_SQL, "Failed to load monthly summary",
            stmt -> {
                stmt.setDate(1, Date.valueOf(startDate));
                stmt.setDate(2, Date.valueOf(endDate));
            },
            JdbcReportRepository::mapMonthlyRow);
    }

    /** Returns per-customer revenue totals across all time. */
    public List<RevenueByCustomerRow> revenueByCustomer() {
        return JdbcSupport.queryList(REVENUE_BY_CUSTOMER_SQL, "Failed to load revenue by customer",
            JdbcReportRepository::mapRevenueByCustomer);
    }

    /** Returns per-category revenue totals across all time. */
    public List<RevenueByCategoryRow> revenueByCategory() {
        return JdbcSupport.queryList(REVENUE_BY_CATEGORY_SQL, "Failed to load revenue by category",
            JdbcReportRepository::mapRevenueByCategory);
    }

    private static CustomerBillLine mapBillLine(ResultSet rs) throws SQLException {
        Date dateLogged = rs.getDate("date_logged");
        return new CustomerBillLine(
            dateLogged == null ? null : dateLogged.toLocalDate(),
            rs.getString("user_name"),
            rs.getString("category_name"),
            nonNull(rs.getBigDecimal("hours")),
            nonNull(rs.getBigDecimal(COL_HOURLY_RATE)),
            nonNull(rs.getBigDecimal("line_total")),
            rs.getString("note"));
    }

    private static MonthlySummaryRow mapMonthlyRow(ResultSet rs) throws SQLException {
        return new MonthlySummaryRow(
            rs.getString("customer_name"),
            nonNull(rs.getBigDecimal(COL_TOTAL_HOURS)),
            nonNull(rs.getBigDecimal("total_amount")));
    }

    private static RevenueByCustomerRow mapRevenueByCustomer(ResultSet rs) throws SQLException {
        return new RevenueByCustomerRow(
            rs.getString(COL_NAME),
            nonNull(rs.getBigDecimal(COL_TOTAL_HOURS)),
            nonNull(rs.getBigDecimal(COL_TOTAL_REVENUE)),
            nonNull(rs.getBigDecimal("avg_rate")));
    }

    private static RevenueByCategoryRow mapRevenueByCategory(ResultSet rs) throws SQLException {
        return new RevenueByCategoryRow(
            rs.getString(COL_NAME),
            nonNull(rs.getBigDecimal(COL_HOURLY_RATE)),
            nonNull(rs.getBigDecimal(COL_TOTAL_HOURS)),
            nonNull(rs.getBigDecimal(COL_TOTAL_REVENUE)));
    }

    /** Aggregates over no rows return SQL NULL; surface them as {@link BigDecimal#ZERO}. */
    private static BigDecimal nonNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
