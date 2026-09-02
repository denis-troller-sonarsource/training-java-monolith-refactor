package com.sourcegraph.demo.bigbadmonolith;

import com.sourcegraph.demo.bigbadmonolith.dao.UserDAO;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Drives {@link StartupListener} through its servlet lifecycle. Runs in its own forked JVM
 * because {@code contextDestroyed()} shuts down the embedded Derby engine process-wide, which
 * would break the unit-test JVM. Methods are ordered so the destructive shutdown runs last.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StartupListenerIT {

    private final StartupListener listener = new StartupListener();

    @Test
    @Order(1)
    void contextInitializedSeedsSampleData() throws SQLException {
        try (InMemoryDatabase db = InMemoryDatabase.createAndInstall()) {
            // A null event is fine: the embedded path does not read from it.
            listener.contextInitialized((ServletContextEvent) null);

            assertThat(new UserDAO().findAll()).isNotEmpty();
        }
    }

    @Test
    @Order(2)
    void contextDestroyedShutsDownEmbeddedDerby() {
        // No Liberty DataSource here, so this takes the embedded branch and shuts Derby down.
        assertThatCode(() -> listener.contextDestroyed((ServletContextEvent) null))
            .doesNotThrowAnyException();
    }
}
