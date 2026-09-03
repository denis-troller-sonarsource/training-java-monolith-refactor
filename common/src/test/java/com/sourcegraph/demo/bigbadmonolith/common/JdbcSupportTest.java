package com.sourcegraph.demo.bigbadmonolith.common;

import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the shared {@link JdbcSupport} helpers against an in-memory database, including the
 * error-translation path (SQLException -> DataAccessException).
 */
class JdbcSupportTest {

    private InMemoryDatabase db;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void insertReturningKeyThenQueryOneRoundTrips() {
        Long id = JdbcSupport.insertReturningKey(
            "INSERT INTO users (email, name) VALUES (?, ?)", "insert failed",
            stmt -> {
                stmt.setString(1, "a@test");
                stmt.setString(2, "Alice");
            });

        assertThat(id).isNotNull();

        String name = JdbcSupport.queryOne(
            "SELECT name FROM users WHERE id = ?", "query failed",
            stmt -> stmt.setLong(1, id),
            rs -> rs.getString("name"));

        assertThat(name).isEqualTo("Alice");
    }

    @Test
    void queryOneReturnsNullWhenNoRow() {
        String name = JdbcSupport.queryOne(
            "SELECT name FROM users WHERE id = ?", "query failed",
            stmt -> stmt.setLong(1, 9999L),
            rs -> rs.getString("name"));

        assertThat(name).isNull();
    }

    @Test
    void queryListReturnsAllRows() {
        JdbcSupport.insertReturningKey("INSERT INTO users (email, name) VALUES (?, ?)", "insert failed",
            stmt -> {
                stmt.setString(1, "a@test");
                stmt.setString(2, "Alice");
            });
        JdbcSupport.insertReturningKey("INSERT INTO users (email, name) VALUES (?, ?)", "insert failed",
            stmt -> {
                stmt.setString(1, "b@test");
                stmt.setString(2, "Bob");
            });

        List<String> names = JdbcSupport.queryList(
            "SELECT name FROM users ORDER BY id", "query failed",
            rs -> rs.getString("name"));

        assertThat(names).containsExactly("Alice", "Bob");
    }

    @Test
    void updateReportsWhetherRowChanged() {
        Long id = JdbcSupport.insertReturningKey("INSERT INTO users (email, name) VALUES (?, ?)", "insert failed",
            stmt -> {
                stmt.setString(1, "a@test");
                stmt.setString(2, "Alice");
            });

        boolean changed = JdbcSupport.update("UPDATE users SET name = ? WHERE id = ?", "update failed",
            stmt -> {
                stmt.setString(1, "Alice II");
                stmt.setLong(2, id);
            });
        boolean noop = JdbcSupport.update("UPDATE users SET name = ? WHERE id = ?", "update failed",
            stmt -> {
                stmt.setString(1, "Nobody");
                stmt.setLong(2, 9999L);
            });

        assertThat(changed).isTrue();
        assertThat(noop).isFalse();
    }

    @Test
    void wrapsSqlExceptionAsDataAccessException() {
        assertThatThrownBy(() -> JdbcSupport.queryList(
            "SELECT * FROM nonexistent_table", "expected failure", rs -> rs.getString(1)))
            .isInstanceOf(DataAccessException.class)
            .hasMessage("expected failure");
    }
}
