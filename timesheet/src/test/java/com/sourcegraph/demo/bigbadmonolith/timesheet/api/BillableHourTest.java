package com.sourcegraph.demo.bigbadmonolith.timesheet.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic accessor coverage for the {@link BillableHour} model, including the no-arg constructor
 * used by frameworks that populate via setters and the java.time date fields.
 */
class BillableHourTest {

    @Test
    void noArgConstructorWithSettersPopulatesFields() {
        LocalDate dateLogged = LocalDate.of(2024, 1, 15);
        Instant createdAt = Instant.parse("2024-01-16T09:30:00Z");

        BillableHour hour = new BillableHour();
        hour.setId(7L);
        hour.setCustomerId(11L);
        hour.setUserId(22L);
        hour.setCategoryId(33L);
        hour.setHours(new BigDecimal("8.50"));
        hour.setNote("Work done");
        hour.setDateLogged(dateLogged);
        hour.setCreatedAt(createdAt);

        assertThat(hour.getId()).isEqualTo(7L);
        assertThat(hour.getCustomerId()).isEqualTo(11L);
        assertThat(hour.getUserId()).isEqualTo(22L);
        assertThat(hour.getCategoryId()).isEqualTo(33L);
        assertThat(hour.getHours()).isEqualByComparingTo(new BigDecimal("8.50"));
        assertThat(hour.getNote()).isEqualTo("Work done");
        assertThat(hour.getDateLogged()).hasToString("2024-01-15");
        assertThat(hour.getCreatedAt()).hasToString("2024-01-16T09:30:00Z");
    }

    @Test
    void convenienceConstructorSetsCreatedAt() {
        LocalDate dateLogged = LocalDate.of(2024, 2, 20);

        BillableHour hour = new BillableHour(11L, 22L, 33L, new BigDecimal("4.00"), "note", dateLogged);

        assertThat(hour.getCustomerId()).isEqualTo(11L);
        assertThat(hour.getUserId()).isEqualTo(22L);
        assertThat(hour.getCategoryId()).isEqualTo(33L);
        assertThat(hour.getHours()).isEqualByComparingTo(new BigDecimal("4.00"));
        assertThat(hour.getNote()).isEqualTo("note");
        assertThat(hour.getDateLogged()).hasToString("2024-02-20");
        assertThat(hour.getCreatedAt()).isNotNull();
    }

    @Test
    void fullConstructorPopulatesEveryField() {
        LocalDate dateLogged = LocalDate.of(2024, 3, 5);
        Instant createdAt = Instant.parse("2024-03-06T12:00:00Z");

        BillableHour hour = new BillableHour(
            5L, 11L, 22L, 33L, new BigDecimal("6.75"), "full", dateLogged, createdAt);

        assertThat(hour.getId()).isEqualTo(5L);
        assertThat(hour.getCustomerId()).isEqualTo(11L);
        assertThat(hour.getUserId()).isEqualTo(22L);
        assertThat(hour.getCategoryId()).isEqualTo(33L);
        assertThat(hour.getHours()).isEqualByComparingTo(new BigDecimal("6.75"));
        assertThat(hour.getNote()).isEqualTo("full");
        assertThat(hour.getDateLogged()).hasToString("2024-03-05");
        assertThat(hour.getCreatedAt()).hasToString("2024-03-06T12:00:00Z");
    }
}
