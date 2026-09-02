package com.sourcegraph.demo.bigbadmonolith.users.service;

import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link DefaultUserService} end to end against the JDBC repository and in-memory Derby,
 * covering the service delegation for every operation.
 */
class DefaultUserServiceTest {

    private InMemoryDatabase db;
    private UserService service;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        service = new DefaultUserService();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void createUserAssignsId() {
        User saved = service.createUser(new User("a@test", "Alice"));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void getUserReturnsCreatedUser() {
        User saved = service.createUser(new User("a@test", "Alice"));

        assertThat(service.getUser(saved.getId()).getName()).isEqualTo("Alice");
    }

    @Test
    void getUserByEmailReturnsCreatedUser() {
        service.createUser(new User("a@test", "Alice"));

        assertThat(service.getUserByEmail("a@test")).isNotNull();
    }

    @Test
    void listUsersReturnsAll() {
        service.createUser(new User("a@test", "Alice"));
        service.createUser(new User("b@test", "Bob"));

        List<User> all = service.listUsers();

        assertThat(all).extracting(User::getEmail).containsExactlyInAnyOrder("a@test", "b@test");
    }

    @Test
    void updateUserChangesName() {
        User saved = service.createUser(new User("a@test", "Alice"));
        saved.setName("Alice II");

        service.updateUser(saved);

        assertThat(service.getUser(saved.getId()).getName()).isEqualTo("Alice II");
    }

    @Test
    void deleteUserRemovesUser() {
        User saved = service.createUser(new User("a@test", "Alice"));

        boolean deleted = service.deleteUser(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(service.getUser(saved.getId())).isNull();
    }
}
