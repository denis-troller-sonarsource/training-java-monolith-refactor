package com.sourcegraph.demo.bigbadmonolith.billing.api;

/**
 * Thrown when a bill is requested for a customer that does not exist.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("Customer not found: " + customerId);
    }
}
