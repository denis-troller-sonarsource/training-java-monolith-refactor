package com.sourcegraph.demo.bigbadmonolith.catalog.repository;

import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryRepository;
import com.sourcegraph.demo.bigbadmonolith.common.DataAccessException;
import com.sourcegraph.demo.bigbadmonolith.common.LibertyConnectionManager;

import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-backed {@link BillingCategoryRepository}. Wraps all {@link SQLException}s in a uniform
 * {@link DataAccessException}.
 */
@ApplicationScoped
public class JdbcBillingCategoryRepository implements BillingCategoryRepository {

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_HOURLY_RATE = "hourly_rate";

    @Override
    public BillingCategory save(BillingCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        String sql = "INSERT INTO billing_categories (name, description, hourly_rate) VALUES (?, ?, ?)";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setBigDecimal(3, category.getHourlyRate());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to save billing category", e);
        }
        return category;
    }

    @Override
    public BillingCategory findById(Long id) {
        String sql = "SELECT id, name, description, hourly_rate FROM billing_categories WHERE id = ?";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find billing category by id", e);
        }
        return null;
    }

    @Override
    public List<BillingCategory> findAll() {
        String sql = "SELECT id, name, description, hourly_rate FROM billing_categories ORDER BY name";
        List<BillingCategory> categories = new ArrayList<>();

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find all billing categories", e);
        }
        return categories;
    }

    @Override
    public boolean update(BillingCategory category) {
        String sql = "UPDATE billing_categories SET name = ?, description = ?, hourly_rate = ? WHERE id = ?";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setBigDecimal(3, category.getHourlyRate());
            stmt.setLong(4, category.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update billing category", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM billing_categories WHERE id = ?";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete billing category", e);
        }
    }

    private static BillingCategory mapRow(ResultSet rs) throws SQLException {
        return new BillingCategory(
            rs.getLong(COL_ID),
            rs.getString(COL_NAME),
            rs.getString(COL_DESCRIPTION),
            rs.getBigDecimal(COL_HOURLY_RATE)
        );
    }
}
