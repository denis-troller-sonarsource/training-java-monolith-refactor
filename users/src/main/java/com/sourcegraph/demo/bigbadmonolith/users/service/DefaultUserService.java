package com.sourcegraph.demo.bigbadmonolith.users.service;

import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserRepository;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;
import com.sourcegraph.demo.bigbadmonolith.users.repository.JdbcUserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Default {@link UserService}, delegating persistence to a {@link UserRepository}.
 */
@ApplicationScoped
public class DefaultUserService implements UserService {

    private final UserRepository userRepository;

    @Inject
    public DefaultUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * No-arg constructor for non-CDI callers reached via {@link java.util.ServiceLoader}
     * (see {@link com.sourcegraph.demo.bigbadmonolith.users.api.Users}). Classpath-mode
     * ServiceLoader requires a public no-arg constructor, so this self-wires the default JDBC
     * repository. Retired once the web layer is fully CDI-managed (Phase 5).
     */
    public DefaultUserService() {
        this(new JdbcUserRepository());
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean deleteUser(Long id) {
        return userRepository.delete(id);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.update(user);
    }
}
