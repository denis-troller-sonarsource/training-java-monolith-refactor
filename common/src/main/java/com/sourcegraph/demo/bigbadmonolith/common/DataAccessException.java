package com.sourcegraph.demo.bigbadmonolith.common;

/**
 * Uniform unchecked exception for data-access failures across all repositories. Replaces the
 * ad-hoc {@code RuntimeException}/checked {@code SQLException} mix the legacy DAOs used, giving
 * callers a single, specific type to catch.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
