package com.sourcegraph.demo.bigbadmonolith.common;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the embedded {@link ConnectionManager}. Exercising it creates the on-disk Derby
 * database under ./data (git-ignored); the static initializer builds the schema on first use.
 */
class ConnectionManagerTest {

    @Test
    void getConnectionReturnsUsableConnection() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isClosed()).isFalse();
        }
    }

    @Test
    void schemaTablesAreCreatedOnFirstUse() throws SQLException {
        // The static initializer runs SchemaDefinition against the embedded DB; verify a table exists.
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customers")) {
            assertThat(rs.next()).isTrue();
        }
    }

    @Test
    void reconnectingReusesExistingSchemaWithoutError() throws SQLException {
        // A second connection proves createTableIfNotExists swallowed the "table exists" state.
        try (Connection first = ConnectionManager.getConnection()) {
            assertThat(first.isClosed()).isFalse();
        }
        try (Connection second = ConnectionManager.getConnection()) {
            assertThat(second.isClosed()).isFalse();
        }
    }
}
