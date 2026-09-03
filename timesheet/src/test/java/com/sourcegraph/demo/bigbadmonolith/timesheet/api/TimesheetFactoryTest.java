package com.sourcegraph.demo.bigbadmonolith.timesheet.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the {@link Timesheet} ServiceLoader bridge resolves the registered
 * {@link BillableHourService} implementation (the temporary entry point for non-CDI callers).
 */
class TimesheetFactoryTest {

    @Test
    void serviceResolvesRegisteredImplementation() {
        BillableHourService service = Timesheet.service();

        assertThat(service).isNotNull();
    }
}
