package com.sourcegraph.demo.bigbadmonolith.common.it;

import org.apache.derby.jdbc.EmbeddedDataSource;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;
import java.util.Hashtable;

/**
 * Test-only JNDI {@link InitialContextFactory} that mimics the Liberty runtime environment.
 *
 * <p>The integration-test JVM is started with {@code java.naming.factory.initial} pointing here, so
 * {@code new InitialContext().lookup("jdbc/DefaultDataSource")} resolves to an in-memory Derby
 * {@link DataSource} instead of failing. This drives {@code LibertyConnectionManager}'s JNDI success
 * path, which is unreachable from plain unit tests.
 */
public final class TestInitialContextFactory implements InitialContextFactory {

    /** The JNDI name Liberty binds the default DataSource under. */
    static final String DATA_SOURCE_JNDI_NAME = "jdbc/DefaultDataSource";

    /** Backs the DataSource with an in-memory Derby database, created on first connection. */
    private static DataSource newInMemoryDataSource() {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:bbm-it");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return new SingleDataSourceContext(DATA_SOURCE_JNDI_NAME, newInMemoryDataSource());
    }
}
