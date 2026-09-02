package com.sourcegraph.demo.bigbadmonolith.users.api;

import java.util.List;

/**
 * Application service for the users context. The public entry point other modules and the web
 * layer use instead of touching the repository directly.
 */
public interface UserService {

    User createUser(User user);

    User getUser(Long id);

    User getUserByEmail(String email);

    List<User> listUsers();

    boolean deleteUser(Long id);

    User updateUser(User user);
}
