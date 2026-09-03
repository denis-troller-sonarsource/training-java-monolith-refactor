package com.sourcegraph.demo.bigbadmonolith.timesheet.api;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic accessor coverage for the {@link BillableHour} model, including the no-arg constructor
 * used by frameworks that populate via setters and the Joda-Time date fields.
 */
class BillableHourTest {

    @Test
    void noArgConstructorWithSettersPopulatesFields() {
        LocalDate dateLogged = new LocalDate(2024, 1, 15);
        DateTime createdAt = new DateTime(2024, 1, 16, 9, 30);

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
        assertThat(hour.getDateLogged()).isEqualTo(dateLogged);
        assertThat(hour.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void convenienceConstructorSetsCreatedAt() {
        LocalDate dateLogged = new LocalDate(2024, 2, 20);

        BillableHour hour = new BillableHour(11L, 22L, 33L, new BigDecimal("4.00"), "note", dateLogged);

        assertThat(hour.getCustomerId()).isEqualTo(11L);
        assertThat(hour.getUserId()).isEqualTo(22L);
        assertThat(hour.getCategoryId()).isEqualTo(33L);
        assertThat(hour.getHours()).isEqualByComparingTo(new BigDecimal("4.00"));
        assertThat(hour.getNote()).isEqualTo("note");
        assertThat(hour.getDateLogged()).isEqualTo(dateLogged);
        assertThat(hour.getCreatedAt()).isNotNull();
    }

    @Test
    void fullConstructorPopulatesEveryField() {
        LocalDate dateLogged = new LocalDate(2024, 3, 5);
        DateTime createdAt = new DateTime(2024, 3, 6, 12, 0);

        BillableHour hour = new BillableHour(
            5L, 11L, 22L, 33L, new BigDecimal("6.75"), "full", dateLogged, createdAt);

        assertThat(hour.getId()).isEqualTo(5L);
        assertThat(hour.getCustomerId()).isEqualTo(11L);
        assertThat(hour.getUserId()).isEqualTo(22L);
        assertThat(hour.getCategoryId()).isEqualTo(33L);
        assertThat(hour.getHours()).isEqualByComparingTo(new BigDecimal("6.75"));
        assertThat(hour.getNote()).isEqualTo("full");
        assertThat(hour.getDateLogged()).isEqualTo(dateLogged);
        assertThat(hour.getCreatedAt()).isEqualTo(createdAt);
    }
}
