package com.sourcegraph.demo.bigbadmonolith.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Small JDBC helper that removes the connection / try-with-resources / exception-wrapping
 * boilerplate repeated across every repository. All methods obtain a connection from
 * {@link LibertyConnectionManager} and translate {@link SQLException} into {@link DataAccessException}
 * with the caller-supplied context message.
 */
public final class JdbcSupport {

    /** Binds parameters onto a prepared statement. */
    @FunctionalInterface
    public interface ParameterBinder {
        void bind(PreparedStatement stmt) throws SQLException;
    }

    /** Maps the current row of a result set to a domain object. */
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private JdbcSupport() {
        // Utility class: no instances.
    }

    /** Returns the first matching row mapped by {@code mapper}, or {@code null} when none. */
    public static <T> T queryOne(String sql, String errorMessage, ParameterBinder binder, RowMapper<T> mapper) {
        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            binder.bind(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapper.map(rs) : null;
            }

        } catch (SQLException e) {
            throw new DataAccessException(errorMessage, e);
        }
    }

    /** Returns every row mapped by {@code mapper}. */
    public static <T> List<T> queryList(String sql, String errorMessage, RowMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(mapper.map(rs));
            }
            return results;

        } catch (SQLException e) {
            throw new DataAccessException(errorMessage, e);
        }
    }

    /** Returns every row matching the bound parameters, mapped by {@code mapper}. */
    public static <T> List<T> queryList(String sql, String errorMessage, ParameterBinder binder, RowMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            binder.bind(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
                return results;
            }

        } catch (SQLException e) {
            throw new DataAccessException(errorMessage, e);
        }
    }

    /** Executes an update/delete and reports whether any row was affected. */
    public static boolean update(String sql, String errorMessage, ParameterBinder binder) {
        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            binder.bind(stmt);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException(errorMessage, e);
        }
    }

    /**
     * Executes an insert and returns the generated key, or {@code null} if none was produced.
     */
    public static Long insertReturningKey(String sql, String errorMessage, ParameterBinder binder) {
        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            binder.bind(stmt);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : null;
            }

        } catch (SQLException e) {
            throw new DataAccessException(errorMessage, e);
        }
    }
}
