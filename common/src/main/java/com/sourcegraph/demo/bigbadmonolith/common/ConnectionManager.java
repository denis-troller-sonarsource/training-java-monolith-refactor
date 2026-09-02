package com.sourcegraph.demo.bigbadmonolith.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Embedded Derby connection provider used for local development and tests (the non-Liberty path).
 * Creates the database and schema on class load.
 */
public final class ConnectionManager {
    private static final String DB_URL = "jdbc:derby:./data/bigbadmonolith;create=true";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "app";

    private ConnectionManager() {
        // Utility class: no instances.
    }

    static {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Derby driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            for (String ddl : SchemaDefinition.CREATE_STATEMENTS) {
                createTableIfNotExists(stmt, ddl);
            }

        } catch (SQLException e) {
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

    public static void shutdown() {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            // Derby signals a successful shutdown by throwing; nothing to do.
        }
    }
}
