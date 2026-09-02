package com.sourcegraph.demo.bigbadmonolith.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for {@link LibertyConnectionManager}'s connection-resolution logic. The JNDI
 * DataSource branch cannot run outside a Jakarta EE container, so these cover the two branches
 * reachable in a plain JVM: the test override and the embedded {@link ConnectionManager} fallback.
 */
class LibertyConnectionManagerTest {

    /** A do-nothing {@link Connection} used only to prove override identity/precedence. */
    private static Connection stubConnection() {
        return (Connection) Proxy.newProxyInstance(
            LibertyConnectionManagerTest.class.getClassLoader(),
            new Class<?>[] {Connection.class},
            (proxy, method, args) -> null);
    }

    @AfterEach
    void clearOverride() {
        LibertyConnectionManager.setConnectionOverride(null);
    }

    @Test
    void noDataSourceIsAvailableOutsideLiberty() {
        assertThat(LibertyConnectionManager.isLibertyDataSourceAvailable()).isFalse();
    }

    @Test
    void getConnectionUsesOverrideWhenSet() throws SQLException {
        Connection stub = stubConnection();
        LibertyConnectionManager.setConnectionOverride(() -> stub);

        Connection result = LibertyConnectionManager.getConnection();

        assertThat(result).isSameAs(stub);
    }

    @Test
    void getConnectionFallsBackToEmbeddedManagerWhenNoOverride() throws SQLException {
        LibertyConnectionManager.setConnectionOverride(null);

        try (Connection conn = LibertyConnectionManager.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isClosed()).isFalse();
        }
    }

    @Test
    void settingNullOverrideRestoresFallback() throws SQLException {
        Connection stub = stubConnection();
        LibertyConnectionManager.setConnectionOverride(() -> stub);
        LibertyConnectionManager.setConnectionOverride(null);

        try (Connection conn = LibertyConnectionManager.getConnection()) {
            assertThat(conn).isNotSameAs(stub);
        }
    }

    @Test
    void initializeDatabaseSchemaIsNoOpWithoutLibertyDataSource() {
        // Outside a container the DataSource is null, so schema init must be a safe no-op
        // (the embedded ConnectionManager owns the schema instead).
        assertThatCode(LibertyConnectionManager::initializeDatabaseSchema)
            .doesNotThrowAnyException();
    }
}
