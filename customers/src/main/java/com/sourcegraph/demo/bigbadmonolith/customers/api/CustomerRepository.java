package com.sourcegraph.demo.bigbadmonolith.customers.api;

import java.util.List;

/**
 * Persistence contract for {@link Customer}s. The public API of the customers context; implemented
 * internally (JDBC) and consumed by {@link CustomerService} and other modules via this interface.
 */
public interface CustomerRepository {

    Customer save(Customer customer);

    Customer findById(Long id);

    List<Customer> findAll();

    boolean update(Customer customer);

    boolean delete(Long id);
}
