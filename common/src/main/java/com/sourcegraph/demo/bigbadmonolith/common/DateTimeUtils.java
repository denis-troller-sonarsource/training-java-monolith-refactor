package com.sourcegraph.demo.bigbadmonolith.common;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Date helpers shared across modules, built on {@code java.time}.
 *
 * <p>The legacy Joda/{@code java.util.Date} formatting and conversion helpers were removed once the
 * codebase migrated to {@code java.time}; only the operations still in use remain.
 */
public final class DateTimeUtils {

    private static final Logger LOGGER = Logger.getLogger(DateTimeUtils.class.getName());

    private DateTimeUtils() {
        // Utility class: no instances.
    }

    /** True for Monday-Friday. */
    public static boolean isWorkingDay(LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek >= 1 && dayOfWeek <= 5;
    }

    /** Returns today's date and logs the request (preserves the legacy side effect). */
    public static LocalDate getCurrentDateAndLog() {
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        LOGGER.log(Level.INFO, "Current date requested: {0}", now);
        return now;
    }
}
