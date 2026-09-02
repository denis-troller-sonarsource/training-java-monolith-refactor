package com.sourcegraph.demo.bigbadmonolith.common;

import java.util.List;

/**
 * Canonical DDL for the billing schema, shared by the embedded {@link ConnectionManager} and the
 * Liberty-managed {@link LibertyConnectionManager} so the two never drift apart.
 */
final class SchemaDefinition {

    private SchemaDefinition() {
        // Constants holder.
    }

    /** Table-creation statements, in dependency order (billable_hours references the others). */
    static final List<String> CREATE_STATEMENTS = List.of(
        """
        CREATE TABLE users (
            id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
            email VARCHAR(255) NOT NULL UNIQUE,
            name VARCHAR(255) NOT NULL,
            PRIMARY KEY (id)
        )
        """,
        """
        CREATE TABLE customers (
            id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
            name VARCHAR(255) NOT NULL,
            email VARCHAR(255) NOT NULL,
            address VARCHAR(500),
            created_at TIMESTAMP NOT NULL,
            PRIMARY KEY (id)
        )
        """,
        """
        CREATE TABLE billing_categories (
            id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
            name VARCHAR(255) NOT NULL,
            description VARCHAR(500),
            hourly_rate DECIMAL(10,2) NOT NULL,
            PRIMARY KEY (id)
        )
        """,
        """
        CREATE TABLE billable_hours (
            id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
            customer_id BIGINT NOT NULL,
            user_id BIGINT NOT NULL,
            category_id BIGINT NOT NULL,
            hours DECIMAL(8,2) NOT NULL,
            note VARCHAR(1000),
            date_logged DATE NOT NULL,
            created_at TIMESTAMP NOT NULL,
            PRIMARY KEY (id),
            FOREIGN KEY (customer_id) REFERENCES customers(id),
            FOREIGN KEY (user_id) REFERENCES users(id),
            FOREIGN KEY (category_id) REFERENCES billing_categories(id)
        )
        """
    );

    /** Derby's SQLState for "table already exists"; treated as a no-op during idempotent creation. */
    static final String TABLE_ALREADY_EXISTS = "X0Y32";
}
