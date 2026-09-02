package com.sourcegraph.demo.bigbadmonolith.users.repository;

import com.sourcegraph.demo.bigbadmonolith.common.DataAccessException;
import com.sourcegraph.demo.bigbadmonolith.common.LibertyConnectionManager;
import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-backed {@link UserRepository}. Wraps all {@link SQLException}s in a uniform
 * {@link DataAccessException}.
 */
@ApplicationScoped
public class JdbcUserRepository implements UserRepository {

    private static final String COL_ID = "id";
    private static final String COL_EMAIL = "email";
    private static final String COL_NAME = "name";

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (email, name) VALUES (?, ?)";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getName());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                    return user;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to save user", e);
        }
    }

    @Override
    public User findById(Long id) {
        String sql = "SELECT id, email, name FROM users WHERE id = ?";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user by id", e);
        }
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT id, email, name FROM users WHERE email = ?";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user by email", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, email, name FROM users ORDER BY id";
        List<User> users = new ArrayList<>();

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Failed to find all users", e);
        }

        return users;
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete user", e);
        }
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, name = ? WHERE id = ?";

        try (Connection conn = LibertyConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getName());
            stmt.setLong(3, user.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }

            return user;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user", e);
        }
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getLong(COL_ID),
            rs.getString(COL_EMAIL),
            rs.getString(COL_NAME)
        );
    }
}
