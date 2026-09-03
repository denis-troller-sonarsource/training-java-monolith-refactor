package com.sourcegraph.demo.bigbadmonolith.catalog.repository;

import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryRepository;
import com.sourcegraph.demo.bigbadmonolith.common.DataAccessException;
import com.sourcegraph.demo.bigbadmonolith.common.JdbcSupport;

import jakarta.enterprise.context.ApplicationScoped;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC-backed {@link BillingCategoryRepository}. Delegates the connection/exception boilerplate to
 * {@link JdbcSupport}, which wraps {@link SQLException} in a uniform {@link DataAccessException}.
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
        Long id = JdbcSupport.insertReturningKey(sql, "Failed to save billing category", stmt -> {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setBigDecimal(3, category.getHourlyRate());
        });
        if (id != null) {
            category.setId(id);
        }
        return category;
    }

    @Override
    public BillingCategory findById(Long id) {
        String sql = "SELECT id, name, description, hourly_rate FROM billing_categories WHERE id = ?";
        return JdbcSupport.queryOne(sql, "Failed to find billing category by id",
            stmt -> stmt.setLong(1, id),
            JdbcBillingCategoryRepository::mapRow);
    }

    @Override
    public List<BillingCategory> findAll() {
        String sql = "SELECT id, name, description, hourly_rate FROM billing_categories ORDER BY name";
        return JdbcSupport.queryList(sql, "Failed to find all billing categories",
            JdbcBillingCategoryRepository::mapRow);
    }

    @Override
    public boolean update(BillingCategory category) {
        String sql = "UPDATE billing_categories SET name = ?, description = ?, hourly_rate = ? WHERE id = ?";
        return JdbcSupport.update(sql, "Failed to update billing category", stmt -> {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setBigDecimal(3, category.getHourlyRate());
            stmt.setLong(4, category.getId());
        });
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM billing_categories WHERE id = ?";
        return JdbcSupport.update(sql, "Failed to delete billing category",
            stmt -> stmt.setLong(1, id));
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
