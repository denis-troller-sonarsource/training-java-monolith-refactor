package com.sourcegraph.demo.bigbadmonolith.testsupport;

import com.sourcegraph.demo.bigbadmonolith.dao.LibertyConnectionManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test harness that backs the DAOs with an isolated in-memory Derby database.
 *
 * <p>Each instance owns a uniquely named {@code jdbc:derby:memory} database, installs a
 * connection override on {@link LibertyConnectionManager} so production DAO code transparently
 * uses it, creates the schema, and tears everything down on {@link #close()}. This lets the
 * characterization suite exercise real SQL without WebSphere Liberty or an on-disk Derby.
 */
public final class InMemoryDatabase implements AutoCloseable {

    private static final AtomicInteger DB_COUNTER = new AtomicInteger();

    private final String baseUrl;

    private InMemoryDatabase(String dbName) {
        this.baseUrl = "jdbc:derby:memory:" + dbName;
    }

    /**
     * Creates a fresh, uniquely named in-memory database, installs it as the active
     * connection source for the DAOs, and creates the schema.
     */
    public static InMemoryDatabase createAndInstall() throws SQLException {
        String name = "bbm-test-" + DB_COUNTER.incrementAndGet();
        InMemoryDatabase db = new InMemoryDatabase(name);
        db.bootstrap();
        LibertyConnectionManager.setConnectionOverride(db::newConnection);
        return db;
    }

    private void bootstrap() throws SQLException {
        // create=true builds the in-memory database on first connect.
        try (Connection conn = DriverManager.getConnection(baseUrl + ";create=true");
             Statement stmt = conn.createStatement()) {
            for (String ddl : Schema.CREATE_STATEMENTS) {
                stmt.addBatch(ddl);
            }
            stmt.executeBatch();
        }
    }

    /** Hands the DAOs a live connection to this test database. */
    public Connection newConnection() throws SQLException {
        return DriverManager.getConnection(baseUrl);
    }

    @Override
    public void close() {
        LibertyConnectionManager.setConnectionOverride(null);
        // drop=true releases the in-memory database; it throws SQLState 08006 by design.
        try {
            DriverManager.getConnection(baseUrl + ";drop=true");
        } catch (SQLException expected) {
            // Derby signals a successful drop with an exception; nothing to do.
        }
    }
}
