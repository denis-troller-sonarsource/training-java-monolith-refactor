package com.sourcegraph.demo.bigbadmonolith.util;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for {@link DateTimeUtils}. Pure unit tests with no database.
 * They lock in the utility's current behavior (including quirks) before refactoring.
 */
class DateTimeUtilsTest {

    @Test
    void formatDateLegacyFormatsWithIsoPattern() {
        String formatted = DateTimeUtils.formatDateLegacy(new LocalDate(2024, 1, 15));

        assertThat(formatted).isEqualTo("2024-01-15");
    }

    @Test
    void formatDateLegacyReturnsEmptyStringForNull() {
        assertThat(DateTimeUtils.formatDateLegacy(null)).isEmpty();
    }

    @Test
    void isWorkingDayReturnsTrueForMonday() {
        // 2024-01-01 is a Monday.
        assertThat(DateTimeUtils.isWorkingDay(new LocalDate(2024, 1, 1))).isTrue();
    }

    @Test
    void isWorkingDayReturnsTrueForFriday() {
        // 2024-01-05 is a Friday.
        assertThat(DateTimeUtils.isWorkingDay(new LocalDate(2024, 1, 5))).isTrue();
    }

    @Test
    void isWorkingDayReturnsFalseForSaturday() {
        // 2024-01-06 is a Saturday.
        assertThat(DateTimeUtils.isWorkingDay(new LocalDate(2024, 1, 6))).isFalse();
    }

    @Test
    void isWorkingDayReturnsFalseForSunday() {
        // 2024-01-07 is a Sunday.
        assertThat(DateTimeUtils.isWorkingDay(new LocalDate(2024, 1, 7))).isFalse();
    }

    @Test
    void convertToSqlDateProducesStartOfDay() {
        LocalDate localDate = new LocalDate(2024, 3, 10);

        Date sqlDate = DateTimeUtils.convertToSqlDate(localDate);

        assertThat(sqlDate.getTime())
            .isEqualTo(localDate.toDateTimeAtStartOfDay().getMillis());
    }

    @Test
    void convertToTimestampPreservesMillis() {
        DateTime dateTime = new DateTime(2024, 3, 10, 14, 30, 45);

        Timestamp timestamp = DateTimeUtils.convertToTimestamp(dateTime);

        assertThat(timestamp.getTime()).isEqualTo(dateTime.getMillis());
    }

    @Test
    void formatDateTimeVerboseFormatsWithZeroPadding() {
        DateTime dateTime = new DateTime(2024, 3, 5, 9, 7, 0);

        String formatted = DateTimeUtils.formatDateTimeVerbose(dateTime);

        assertThat(formatted).isEqualTo("2024-03-05 09:07");
    }

    @Test
    void formatDateTimeVerboseThrowsForNull() {
        assertThatThrownBy(() -> DateTimeUtils.formatDateTimeVerbose(null))
            .isInstanceOf(RuntimeException.class);
    }
}
