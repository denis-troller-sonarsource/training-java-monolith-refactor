package com.sourcegraph.demo.bigbadmonolith.common;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Date/time helpers shared across modules.
 *
 * <p>Still built on Joda-Time because the entities and services that call it use Joda types;
 * the migration to {@code java.time} happens when those callers move into their own modules.
 * Method contracts here are intentionally preserved (return values on null/error inputs) so the
 * characterization suite keeps passing.
 */
public final class DateTimeUtils {

    private static final Logger LOGGER = Logger.getLogger(DateTimeUtils.class.getName());

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final SimpleDateFormat LEGACY_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    private DateTimeUtils() {
        // Utility class: no instances.
    }

    public static String formatDateLegacy(LocalDate date) {
        if (date == null) {
            return "";
        }
        try {
            return date.toString(DATE_FORMATTER);
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Failed to format date", e);
            return "Invalid Date";
        }
    }

    public static String formatDateTimeVerbose(DateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("DateTime cannot be null");
        }
        try {
            String year = String.valueOf(dateTime.getYear());
            String month = pad(dateTime.getMonthOfYear());
            String day = pad(dateTime.getDayOfMonth());
            String hour = pad(dateTime.getHourOfDay());
            String minute = pad(dateTime.getMinuteOfHour());
            return year + "-" + month + "-" + day + " " + hour + ":" + minute;
        } catch (RuntimeException re) {
            return null;
        }
    }

    private static String pad(int value) {
        return value < 10 ? "0" + value : String.valueOf(value);
    }

    public static java.util.Date convertToJavaUtilDate(DateTime jodaDateTime) {
        return new java.util.Date(jodaDateTime.getMillis());
    }

    public static Timestamp convertToTimestamp(DateTime dateTime) {
        return new Timestamp(dateTime.getMillis());
    }

    public static Date convertToSqlDate(LocalDate localDate) {
        return new Date(localDate.toDateTimeAtStartOfDay().getMillis());
    }

    public static boolean isWorkingDay(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek();
        return dayOfWeek >= 1 && dayOfWeek <= 5;
    }

    public static String formatForDisplay(DateTime dateTime) {
        synchronized (LEGACY_DATE_FORMAT) {
            return LEGACY_DATE_FORMAT.format(convertToJavaUtilDate(dateTime));
        }
    }

    public static LocalDate getCurrentDateAndLog() {
        LocalDate now = LocalDate.now();
        LOGGER.log(Level.INFO, "Current date requested: {0}", now);
        return now;
    }
}
