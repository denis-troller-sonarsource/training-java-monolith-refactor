package com.sourcegraph.demo.bigbadmonolith;

import com.sourcegraph.demo.bigbadmonolith.catalog.repository.JdbcBillingCategoryRepository;
import com.sourcegraph.demo.bigbadmonolith.catalog.service.DefaultBillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.customers.repository.JdbcCustomerRepository;
import com.sourcegraph.demo.bigbadmonolith.customers.service.DefaultCustomerService;
import com.sourcegraph.demo.bigbadmonolith.service.DataInitializationService;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import com.sourcegraph.demo.bigbadmonolith.timesheet.repository.JdbcBillableHourRepository;
import com.sourcegraph.demo.bigbadmonolith.timesheet.service.DefaultBillableHourService;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;
import com.sourcegraph.demo.bigbadmonolith.users.repository.JdbcUserRepository;
import com.sourcegraph.demo.bigbadmonolith.users.service.DefaultUserService;
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
 *
 * <p>On Liberty the {@link DataInitializationService} constructor argument is supplied by CDI.
 * Outside a container this test wires an equivalent instance so the lifecycle callbacks can run
 * against the embedded Derby stack.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StartupListenerIT {

    private final UserService userService = new DefaultUserService(new JdbcUserRepository());
    private final StartupListener listener = new StartupListener(new DataInitializationService(
        userService,
        new DefaultCustomerService(new JdbcCustomerRepository()),
        new DefaultBillingCategoryService(new JdbcBillingCategoryRepository()),
        new DefaultBillableHourService(new JdbcBillableHourRepository())));

    @Test
    @Order(1)
    void contextInitializedSeedsSampleData() throws SQLException {
        try (InMemoryDatabase db = InMemoryDatabase.createAndInstall()) {
            // A null event is fine: the embedded path does not read from it.
            listener.contextInitialized((ServletContextEvent) null);

            assertThat(userService.listUsers()).isNotEmpty();
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
