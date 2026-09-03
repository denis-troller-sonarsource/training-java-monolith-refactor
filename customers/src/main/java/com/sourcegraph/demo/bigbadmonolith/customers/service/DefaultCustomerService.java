package com.sourcegraph.demo.bigbadmonolith.customers.service;

import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerRepository;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;

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
