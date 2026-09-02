package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.entity.User;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization tests for {@link DatabaseService}, the thin user-facing facade over
 * {@code UserDAO}. Locks in its delegation behavior before the modular refactoring.
 */
class DatabaseServiceTest {

    private InMemoryDatabase db;
    private DatabaseService service;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        service = new DatabaseService();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void saveUserAssignsId() {
        User saved = service.saveUser(new User("a@test", "Alice"));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findUserByIdReturnsSavedUser() {
        User saved = service.saveUser(new User("a@test", "Alice"));

        User found = service.findUserById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Alice");
    }

    @Test
    void findUserByEmailReturnsSavedUser() {
        service.saveUser(new User("a@test", "Alice"));

        User found = service.findUserByEmail("a@test");

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("a@test");
    }

    @Test
    void findAllUsersReturnsEverySavedUser() {
        service.saveUser(new User("a@test", "Alice"));
        service.saveUser(new User("b@test", "Bob"));

        List<User> all = service.findAllUsers();

        assertThat(all).extracting(User::getEmail).containsExactlyInAnyOrder("a@test", "b@test");
    }

    @Test
    void updateUserChangesName() {
        User saved = service.saveUser(new User("a@test", "Alice"));
        saved.setName("Alice Renamed");

        service.updateUser(saved);

        assertThat(service.findUserById(saved.getId()).getName()).isEqualTo("Alice Renamed");
    }

    @Test
    void deleteUserRemovesUser() {
        User saved = service.saveUser(new User("a@test", "Alice"));

        boolean deleted = service.deleteUser(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(service.findUserById(saved.getId())).isNull();
    }
}
