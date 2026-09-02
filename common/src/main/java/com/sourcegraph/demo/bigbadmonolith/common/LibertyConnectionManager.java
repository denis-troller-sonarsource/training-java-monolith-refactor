package com.sourcegraph.demo.bigbadmonolith.common;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resolves JDBC connections, preferring a Liberty-managed JNDI {@link DataSource} and falling back
 * to the embedded {@link ConnectionManager} for local development and tests. A {@link
 * ConnectionSupplier} override takes precedence over both, letting the repositories run against an
 * in-memory database without a Jakarta EE container.
 */
public final class LibertyConnectionManager {
    private static final Logger LOGGER = Logger.getLogger(LibertyConnectionManager.class.getName());
    private static final String JNDI_NAME = "jdbc/DefaultDataSource";
    private static DataSource dataSource;

    /**
     * Optional override for supplying connections. When set (e.g. by tests), it takes
     * precedence over the JNDI DataSource and the embedded {@link ConnectionManager}.
     */
    private static ConnectionSupplier connectionOverride;

    /** Supplies JDBC connections; separate type so it can declare {@link SQLException}. */
    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection getConnection() throws SQLException;
    }

    private LibertyConnectionManager() {
        // Utility class: no instances.
    }

    static {
        try {
            initializeDataSource();
        } catch (Exception e) {
            // Fall back to the embedded ConnectionManager for development.
            LOGGER.log(Level.WARNING, "Failed to initialize Liberty DataSource, falling back to embedded Derby", e);
        }
    }

    private static void initializeDataSource() throws NamingException {
        InitialContext ctx = new InitialContext();
        dataSource = (DataSource) ctx.lookup(JNDI_NAME);
        LOGGER.log(Level.INFO, "Successfully initialized Liberty DataSource from JNDI: {0}", JNDI_NAME);
    }

    /**
     * Installs a connection override. Intended for tests. Pass {@code null} to clear it.
     */
    public static void setConnectionOverride(ConnectionSupplier override) {
        connectionOverride = override;
    }

    public static Connection getConnection() throws SQLException {
        if (connectionOverride != null) {
            return connectionOverride.getConnection();
        }
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        // Fall back to the embedded ConnectionManager.
        return ConnectionManager.getConnection();
    }

    public static boolean isLibertyDataSourceAvailable() {
        return dataSource != null;
    }

    /** Creates the schema when running on a Liberty DataSource; a no-op otherwise. */
    public static void initializeDatabaseSchema() {
        if (!isLibertyDataSourceAvailable()) {
            return; // Let the embedded ConnectionManager handle this.
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            for (String ddl : SchemaDefinition.CREATE_STATEMENTS) {
                createTableIfNotExists(stmt, ddl);
            }

            LOGGER.info("Database schema initialized successfully via Liberty DataSource");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema via Liberty DataSource", e);
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private static void createTableIfNotExists(Statement stmt, String createTableSQL) throws SQLException {
        try {
            stmt.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            // Table might already exist, ignore that specific error.
            if (!e.getSQLState().equals(SchemaDefinition.TABLE_ALREADY_EXISTS)) {
                throw e;
            }
        }
    }
}
