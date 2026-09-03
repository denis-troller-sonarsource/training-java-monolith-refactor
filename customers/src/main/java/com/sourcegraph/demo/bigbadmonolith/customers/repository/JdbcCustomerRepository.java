package com.sourcegraph.demo.bigbadmonolith.customers.repository;

import com.sourcegraph.demo.bigbadmonolith.common.DataAccessException;
import com.sourcegraph.demo.bigbadmonolith.common.LibertyConnectionManager;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerRepository;

import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-backed {@link CustomerRepository}. Wraps all {@link SQLException}s in a uniform
 * {@link DataAccessException}.
 */
@ApplicationScoped
public class JdbcCustomerRepository implements CustomerRepository {

    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_ADDRESS = "address";
    private static final String COL_CREATED_AT = "created_at";

    @Override
    public Customer save(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be null or empty");
        }
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email cannot be null or empty");
        }

        String sql = "INSERT INTO customers (name, email, address, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getAddress());
            Instant createdAt = customer.getCreatedAt() != null ? customer.getCreatedAt() : Instant.now();
            stmt.setTimestamp(4, Timestamp.from(createdAt));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getLong(1));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to save customer", e);
        }
        return customer;
    }

    @Override
    public Customer findById(Long id) {
        String sql = "SELECT id, name, email, address, created_at FROM customers WHERE id = ?";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find customer by id", e);
        }
        return null;
    }

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT id, name, email, address, created_at FROM customers ORDER BY created_at DESC";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find all customers", e);
        }
        return customers;
    }

    @Override
    public boolean update(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be null or empty");
        }
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email cannot be null or empty");
        }

        String sql = "UPDATE customers SET name = ?, email = ?, address = ? WHERE id = ?";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getAddress());
            stmt.setLong(4, customer.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update customer", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete customer", e);
        }
    }

    private static Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getLong(COL_ID),
            rs.getString(COL_NAME),
            rs.getString(COL_EMAIL),
            rs.getString(COL_ADDRESS),
            rs.getTimestamp(COL_CREATED_AT).toInstant()
        );
    }
}
