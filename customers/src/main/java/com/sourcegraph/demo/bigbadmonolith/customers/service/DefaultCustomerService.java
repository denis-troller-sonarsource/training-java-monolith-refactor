package com.sourcegraph.demo.bigbadmonolith.customers.service;

import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerRepository;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;
import com.sourcegraph.demo.bigbadmonolith.customers.repository.JdbcCustomerRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Default {@link CustomerService}, delegating persistence to a {@link CustomerRepository}.
 */
@ApplicationScoped
public class DefaultCustomerService implements CustomerService {

    private final CustomerRepository customerRepository;

    @Inject
    public DefaultCustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * No-arg constructor for non-CDI callers reached via {@link java.util.ServiceLoader}
     * (see {@link com.sourcegraph.demo.bigbadmonolith.customers.api.Customers}). Classpath-mode
     * ServiceLoader requires a public no-arg constructor, so this self-wires the default JDBC
     * repository. Retired once the web layer is fully CDI-managed (Phase 5).
     */
    public DefaultCustomerService() {
        this(new JdbcCustomerRepository());
    }

    @Override
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Customer getCustomer(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public boolean updateCustomer(Customer customer) {
        return customerRepository.update(customer);
    }

    @Override
    public boolean deleteCustomer(Long id) {
        return customerRepository.delete(id);
    }
}
