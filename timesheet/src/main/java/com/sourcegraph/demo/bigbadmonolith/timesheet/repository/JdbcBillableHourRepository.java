package com.sourcegraph.demo.bigbadmonolith.timesheet.repository;

import com.sourcegraph.demo.bigbadmonolith.common.DataAccessException;
import com.sourcegraph.demo.bigbadmonolith.common.JdbcSupport;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourRepository;

import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * JDBC-backed {@link BillableHourRepository}. Delegates the connection/exception boilerplate to
 * {@link JdbcSupport}, which wraps {@link SQLException} in a uniform {@link DataAccessException}.
 */
@ApplicationScoped
public class JdbcBillableHourRepository implements BillableHourRepository {

    private static final String COL_ID = "id";
    private static final String COL_CUSTOMER_ID = "customer_id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_CATEGORY_ID = "category_id";
    private static final String COL_HOURS = "hours";
    private static final String COL_NOTE = "note";
    private static final String COL_DATE_LOGGED = "date_logged";
    private static final String COL_CREATED_AT = "created_at";

    @Override
    public BillableHour save(BillableHour billableHour) {
        String sql = "INSERT INTO billable_hours (customer_id, user_id, category_id, hours, note, date_logged, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Long id = JdbcSupport.insertReturningKey(sql, "Failed to save billable hour", stmt -> {
            stmt.setLong(1, billableHour.getCustomerId());
            stmt.setLong(2, billableHour.getUserId());
            stmt.setLong(3, billableHour.getCategoryId());
            stmt.setBigDecimal(4, billableHour.getHours());
            stmt.setString(5, billableHour.getNote());
            stmt.setDate(6, Date.valueOf(billableHour.getDateLogged()));
            stmt.setTimestamp(7, Timestamp.from(billableHour.getCreatedAt() != null ? billableHour.getCreatedAt() : Instant.now()));
        });
        if (id != null) {
            billableHour.setId(id);
        }
        return billableHour;
    }

    @Override
    public BillableHour findById(Long id) {
        String sql = "SELECT id, customer_id, user_id, category_id, hours, note, date_logged, created_at FROM billable_hours WHERE id = ?";
        return JdbcSupport.queryOne(sql, "Failed to find billable hour by id",
            stmt -> stmt.setLong(1, id),
            JdbcBillableHourRepository::mapRow);
    }

    @Override
    public List<BillableHour> findByCustomerId(Long customerId) {
        String sql = "SELECT id, customer_id, user_id, category_id, hours, note, date_logged, created_at FROM billable_hours WHERE customer_id = ? ORDER BY date_logged DESC";
        return JdbcSupport.queryList(sql, "Failed to find billable hours by customer id",
            stmt -> stmt.setLong(1, customerId),
            JdbcBillableHourRepository::mapRow);
    }

    @Override
    public List<BillableHour> findByUserId(Long userId) {
        String sql = "SELECT id, customer_id, user_id, category_id, hours, note, date_logged, created_at FROM billable_hours WHERE user_id = ? ORDER BY date_logged DESC";
        return JdbcSupport.queryList(sql, "Failed to find billable hours by user id",
            stmt -> stmt.setLong(1, userId),
            JdbcBillableHourRepository::mapRow);
    }

    @Override
    public List<BillableHour> findAll() {
        String sql = "SELECT id, customer_id, user_id, category_id, hours, note, date_logged, created_at FROM billable_hours ORDER BY date_logged DESC";
        return JdbcSupport.queryList(sql, "Failed to find all billable hours",
            JdbcBillableHourRepository::mapRow);
    }

    @Override
    public boolean update(BillableHour billableHour) {
        String sql = "UPDATE billable_hours SET customer_id = ?, user_id = ?, category_id = ?, hours = ?, note = ?, date_logged = ? WHERE id = ?";
        return JdbcSupport.update(sql, "Failed to update billable hour", stmt -> {
            stmt.setLong(1, billableHour.getCustomerId());
            stmt.setLong(2, billableHour.getUserId());
            stmt.setLong(3, billableHour.getCategoryId());
            stmt.setBigDecimal(4, billableHour.getHours());
            stmt.setString(5, billableHour.getNote());
            stmt.setDate(6, Date.valueOf(billableHour.getDateLogged()));
            stmt.setLong(7, billableHour.getId());
        });
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM billable_hours WHERE id = ?";
        return JdbcSupport.update(sql, "Failed to delete billable hour",
            stmt -> stmt.setLong(1, id));
    }

    private static BillableHour mapRow(ResultSet rs) throws SQLException {
        return new BillableHour(
            rs.getLong(COL_ID),
            rs.getLong(COL_CUSTOMER_ID),
            rs.getLong(COL_USER_ID),
            rs.getLong(COL_CATEGORY_ID),
            rs.getBigDecimal(COL_HOURS),
            rs.getString(COL_NOTE),
            rs.getDate(COL_DATE_LOGGED).toLocalDate(),
            rs.getTimestamp(COL_CREATED_AT).toInstant()
        );
    }
}
