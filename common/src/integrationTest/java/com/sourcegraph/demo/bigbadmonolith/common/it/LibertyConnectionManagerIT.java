package com.sourcegraph.demo.bigbadmonolith.common.it;

import com.sourcegraph.demo.bigbadmonolith.common.LibertyConnectionManager;
import org.junit.jupiter.api.AfterAll;
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
 * Integration tests for {@link LibertyConnectionManager}'s container (JNDI) code paths.
 *
 * <p>This JVM is launched with {@code java.naming.factory.initial} pointing at
 * {@link TestInitialContextFactory}, so the very first reference to {@link LibertyConnectionManager}
 * triggers its static initializer to resolve an in-memory Derby DataSource from JNDI. That makes the
 * JNDI success path, the {@code dataSource.getConnection()} branch, and the Liberty schema-init path
 * reachable here in a way plain unit tests cannot achieve.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LibertyConnectionManagerIT {

    @AfterAll
    static void clearOverride() {
        LibertyConnectionManager.setConnectionOverride(null);
    }

    @Test
    @Order(1)
    void libertyDataSourceIsResolvedFromJndi() {
        assertThat(LibertyConnectionManager.isLibertyDataSourceAvailable()).isTrue();
    }

    @Test
    @Order(2)
    void getConnectionReturnsUsableConnectionFromDataSource() throws SQLException {
        LibertyConnectionManager.setConnectionOverride(null);

        try (Connection conn = LibertyConnectionManager.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isClosed()).isFalse();
        }
    }

    @Test
    @Order(3)
    void initializeDatabaseSchemaCreatesSchemaOnLibertyDataSource() {
        assertThatCode(LibertyConnectionManager::initializeDatabaseSchema)
            .doesNotThrowAnyException();
    }

    @Test
    @Order(4)
    void schemaTablesExistAfterLibertyInitialization() throws SQLException {
        LibertyConnectionManager.initializeDatabaseSchema();

        try (Connection conn = LibertyConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customers")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isZero();
        }
    }

    @Test
    @Order(5)
    void reinitializingSchemaIsIdempotent() {
        // A second run exercises the "table already exists" branch of createTableIfNotExists.
        assertThatCode(LibertyConnectionManager::initializeDatabaseSchema)
            .doesNotThrowAnyException();
    }
}
