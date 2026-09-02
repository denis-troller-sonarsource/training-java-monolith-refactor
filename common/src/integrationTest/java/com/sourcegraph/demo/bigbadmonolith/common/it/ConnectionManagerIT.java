package com.sourcegraph.demo.bigbadmonolith.common.it;

import com.sourcegraph.demo.bigbadmonolith.common.ConnectionManager;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Integration tests for the embedded {@link ConnectionManager}, including {@link
 * ConnectionManager#shutdown()} which tears down the whole Derby engine and therefore must run last.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConnectionManagerIT {

    @Test
    @Order(1)
    void getConnectionReturnsUsableConnection() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isClosed()).isFalse();
        }
    }

    @Test
    @Order(2)
    void schemaTablesAreCreatedOnFirstUse() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customers")) {
            assertThat(rs.next()).isTrue();
        }
    }

    @Test
    @Order(3)
    void reconnectingReusesExistingSchemaWithoutError() throws SQLException {
        // A second connection proves createTableIfNotExists swallowed the "table exists" state.
        try (Connection first = ConnectionManager.getConnection()) {
            assertThat(first.isClosed()).isFalse();
        }
        try (Connection second = ConnectionManager.getConnection()) {
            assertThat(second.isClosed()).isFalse();
        }
    }

    @Test
    @Order(Integer.MAX_VALUE)
    void shutdownStopsDerbyWithoutThrowing() {
        // Runs last: Derby signals a successful shutdown by throwing internally, which
        // ConnectionManager.shutdown() must swallow so the caller sees no exception.
        assertThatCode(ConnectionManager::shutdown).doesNotThrowAnyException();
    }
}
