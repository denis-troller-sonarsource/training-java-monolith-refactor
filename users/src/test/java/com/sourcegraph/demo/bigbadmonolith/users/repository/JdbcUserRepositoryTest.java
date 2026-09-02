package com.sourcegraph.demo.bigbadmonolith.users.repository;

import com.sourcegraph.demo.bigbadmonolith.common.DataAccessException;
import com.sourcegraph.demo.bigbadmonolith.testsupport.InMemoryDatabase;
import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for {@link JdbcUserRepository} against a real in-memory Derby database.
 * These lock in the repository's CRUD behavior, including wrapping {@link SQLException} in a
 * {@link DataAccessException}.
 */
class JdbcUserRepositoryTest {

    private InMemoryDatabase db;
    private JdbcUserRepository dao;

    @BeforeEach
    void setUp() throws SQLException {
        db = InMemoryDatabase.createAndInstall();
        dao = new JdbcUserRepository();
    }

    @AfterEach
    void tearDown() {
        db.close();
    }

    @Test
    void savePopulatesGeneratedId() {
        User saved = dao.save(new User("john.doe@example.com", "John Doe"));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findByIdReturnsSavedUser() {
        User saved = dao.save(new User("john.doe@example.com", "John Doe"));

        User found = dao.findById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(found.getName()).isEqualTo("John Doe");
    }

    @Test
    void findByIdReturnsNullWhenAbsent() {
        assertThat(dao.findById(9999L)).isNull();
    }

    @Test
    void findByEmailReturnsSavedUser() {
        dao.save(new User("jane.smith@example.com", "Jane Smith"));

        User found = dao.findByEmail("jane.smith@example.com");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Jane Smith");
    }

    @Test
    void findByEmailReturnsNullWhenAbsent() {
        assertThat(dao.findByEmail("missing@example.com")).isNull();
    }

    @Test
    void findAllOrdersByIdAscending() {
        dao.save(new User("first@example.com", "First"));
        dao.save(new User("second@example.com", "Second"));

        List<User> all = dao.findAll();

        assertThat(all).extracting(User::getName).containsExactly("First", "Second");
    }

    @Test
    void updateChangesPersistedFields() {
        User saved = dao.save(new User("john.doe@example.com", "John Doe"));
        saved.setEmail("john.updated@example.com");
        saved.setName("John Updated");

        User updated = dao.update(saved);

        assertThat(updated).isNotNull();
        User reloaded = dao.findById(saved.getId());
        assertThat(reloaded.getEmail()).isEqualTo("john.updated@example.com");
        assertThat(reloaded.getName()).isEqualTo("John Updated");
    }

    @Test
    void deleteRemovesUser() {
        User saved = dao.save(new User("john.doe@example.com", "John Doe"));

        boolean deleted = dao.delete(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(dao.findById(saved.getId())).isNull();
    }

    @Test
    void saveWrapsDuplicateEmailInDataAccessException() {
        dao.save(new User("dup@example.com", "First"));
        User duplicate = new User("dup@example.com", "Second");

        assertThatThrownBy(() -> dao.save(duplicate))
            .isInstanceOf(DataAccessException.class);
    }
}
