package com.sourcegraph.demo.bigbadmonolith.users.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic accessor coverage for the {@link User} model, including the no-arg constructor used by
 * frameworks that populate via setters.
 */
class UserTest {

    @Test
    void noArgConstructorWithSettersPopulatesFields() {
        User user = new User();
        user.setId(7L);
        user.setEmail("a@test");
        user.setName("Alice");

        assertThat(user.getId()).isEqualTo(7L);
        assertThat(user.getEmail()).isEqualTo("a@test");
        assertThat(user.getName()).isEqualTo("Alice");
    }
}
