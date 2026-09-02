package com.sourcegraph.demo.bigbadmonolith.users.api;

import java.util.List;

/**
 * Persistence contract for {@link User}s. The public API of the users context; implemented
 * internally (JDBC) and consumed by {@link UserService} and other modules via this interface.
 */
public interface UserRepository {

    User save(User user);

    User findById(Long id);

    User findByEmail(String email);

    List<User> findAll();

    boolean delete(Long id);

    User update(User user);
}
