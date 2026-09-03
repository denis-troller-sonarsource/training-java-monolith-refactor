package com.sourcegraph.demo.bigbadmonolith.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DateTimeUtils}, covering the working-day check and the current-date helper.
 */
class DateTimeUtilsTest {

    @Test
    void isWorkingDayReturnsTrueForMonday() {
        // 2024-01-01 is a Monday.
        assertThat(DateTimeUtils.isWorkingDay(LocalDate.of(2024, 1, 1))).isTrue();
    }

    @Test
    void isWorkingDayReturnsTrueForFriday() {
        // 2024-01-05 is a Friday.
        assertThat(DateTimeUtils.isWorkingDay(LocalDate.of(2024, 1, 5))).isTrue();
    }

    @Test
    void isWorkingDayReturnsFalseForSaturday() {
        // 2024-01-06 is a Saturday.
        assertThat(DateTimeUtils.isWorkingDay(LocalDate.of(2024, 1, 6))).isFalse();
    }

    @Test
    void isWorkingDayReturnsFalseForSunday() {
        // 2024-01-07 is a Sunday.
        assertThat(DateTimeUtils.isWorkingDay(LocalDate.of(2024, 1, 7))).isFalse();
    }

    @Test
    void getCurrentDateAndLogReturnsToday() {
        LocalDate now = DateTimeUtils.getCurrentDateAndLog();

        assertThat(now).isEqualTo(LocalDate.now(ZoneId.systemDefault()));
    }
}
