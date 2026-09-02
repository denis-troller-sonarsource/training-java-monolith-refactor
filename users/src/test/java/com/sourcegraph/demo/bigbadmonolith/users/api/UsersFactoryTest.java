package com.sourcegraph.demo.bigbadmonolith.users.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the {@link Users} ServiceLoader bridge resolves the registered {@link UserService}
 * implementation (the temporary entry point for non-CDI callers).
 */
class UsersFactoryTest {

    @Test
    void serviceResolvesRegisteredImplementation() {
        UserService service = Users.service();

        assertThat(service).isNotNull();
    }
}
