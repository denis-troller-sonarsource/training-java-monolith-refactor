package com.sourcegraph.demo.bigbadmonolith.customers.api;

import java.util.List;

/**
 * Application service for the customers context. The public entry point other modules and the web
 * layer use instead of touching the repository directly.
 */
public interface CustomerService {

    Customer createCustomer(Customer customer);

    Customer getCustomer(Long id);

    List<Customer> listCustomers();

    boolean updateCustomer(Customer customer);

    boolean deleteCustomer(Long id);
}
