package com.sourcegraph.demo.bigbadmonolith.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the shared {@link DataAccessException} preserves message and cause for both
 * constructor forms.
 */
class DataAccessExceptionTest {

    @Test
    void carriesMessageAndCause() {
        Throwable cause = new IllegalStateException("boom");

        DataAccessException ex = new DataAccessException("failed", cause);

        assertThat(ex).hasMessage("failed").hasCause(cause);
    }

    @Test
    void carriesMessageOnly() {
        DataAccessException ex = new DataAccessException("failed");

        assertThat(ex).hasMessage("failed").hasNoCause();
    }
}
