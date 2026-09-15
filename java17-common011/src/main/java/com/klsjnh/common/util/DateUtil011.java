package com.klsjnh.common.util;

/*                DateUtil011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  date util 011 class
 *
 */

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Date and time helpers — the single home for every date/time concern in the
 * framework. Pure Java shared kernel, usable from any layer including the
 * domain layer.
 * <p>
 * Callers MUST NOT build their own {@link DateTimeFormatter} or call
 * {@code System.currentTimeMillis()} directly: patterns are owned here so a
 * format change lands in one place, and the clock is funneled through one
 * method so it can be observed or replaced as a whole.
 * </p>
 */

public final class DateUtil011 {

    /**
     * Compact timestamp: {@code yyyyMMdd_HHmmss}, e.g. {@code 20260915_164230}.
     * Used for backup object keys and other name-embedded stamps.
     */
    public static final String PATTERN_STAMP = "yyyyMMdd_HHmmss";

    /**
     * Date only: {@code yyyy-MM-dd}.
     */
    public static final String PATTERN_DATE = "yyyy-MM-dd";

    /**
     * Date and time: {@code yyyy-MM-dd HH:mm:ss}.
     */
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * Time only: {@code HH:mm:ss}.
     */
    public static final String PATTERN_TIME = "HH:mm:ss";

    /**
     * Month bucket: {@code yyyyMM}.
     */
    public static final String PATTERN_MONTH = "yyyyMM";

    /**
     * Day bucket: {@code yyyyMMdd}.
     */
    public static final String PATTERN_DAY = "yyyyMMdd";

    /**
     * Thread safe stamp formatter for {@link #PATTERN_STAMP}.
     */
    private static final DateTimeFormatter STAMP_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_STAMP);

    /**
     * Thread safe date formatter for {@link #PATTERN_DATE}.
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_DATE);

    /**
     * Thread safe date time formatter for {@link #PATTERN_DATETIME}.
     */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(PATTERN_DATETIME);

    /**
     * All patterns this utility owns, for callers that need to iterate them.
     */
    private static final List<String> PATTERNS =
            List.of(PATTERN_STAMP, PATTERN_DATE, PATTERN_DATETIME, PATTERN_TIME, PATTERN_MONTH, PATTERN_DAY);

    /**
     * Utility class, no instances.
     */
    private DateUtil011() {
    }

    /**
     * Current server time.
     *
     * @return current local date and time, never null
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Current server time in epoch milliseconds, the single clock source for
     * response envelopes and audit stamps.
     *
     * @return current epoch milliseconds
     */
    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Current stamp in {@link #PATTERN_STAMP}, e.g. {@code 20260915_164230}.
     *
     * @return compact stamp for the current server time
     */
    public static String nowStamp() {
        return stamp(now());
    }

    /**
     * Format a date time with {@link #PATTERN_STAMP}.
     *
     * @param dateTime date time, may be null
     * @return stamped text, or an empty string when the value is null
     */
    public static String stamp(LocalDateTime dateTime) {
        return dateTime == null ? "" : STAMP_FORMATTER.format(dateTime);
    }

    /**
     * Format a date time with {@link #PATTERN_DATETIME}.
     *
     * @param dateTime date time, may be null
     * @return formatted text, or an empty string when the value is null
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATETIME_FORMATTER.format(dateTime);
    }

    /**
     * Format a date with {@link #PATTERN_DATE}.
     *
     * @param date date, may be null
     * @return formatted text, or an empty string when the value is null
     */
    public static String format(LocalDate date) {
        return date == null ? "" : DATE_FORMATTER.format(date);
    }

    /**
     * Format a date time with a caller supplied pattern. Prefer one of the
     * named patterns when it fits, so the format stays owned here.
     *
     * @param dateTime date time, may be null
     * @param pattern  pattern accepted by {@link DateTimeFormatter}
     * @return formatted text, or an empty string when the value or pattern is
     *         null
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || StringUtil011.isBlank(pattern)) {
            return "";
        }

        return DateTimeFormatter.ofPattern(pattern).format(dateTime);
    }

    /**
     * Parse text with {@link #PATTERN_DATETIME}.
     *
     * @param text text to parse
     * @return parsed date time, or null when the text is blank or malformed
     */
    public static LocalDateTime parse(String text) {
        return parse(text, PATTERN_DATETIME);
    }

    /**
     * Parse an ISO-8601 date time, the format Spring deserializes
     * {@code LocalDateTime} request fields from.
     *
     * @param text ISO-8601 date time text
     * @return parsed date time, or null when the text is blank or malformed
     */
    public static LocalDateTime parseIso(String text) {
        if (StringUtil011.isBlank(text)) {
            return null;
        }

        try {
            return LocalDateTime.parse(text.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /**
     * Parse text with a caller supplied pattern.
     *
     * @param text    text to parse
     * @param pattern pattern accepted by {@link DateTimeFormatter}
     * @return parsed date time, or null when the text is blank, the pattern is
     *         blank, or the text does not match the pattern
     */
    public static LocalDateTime parse(String text, String pattern) {
        if (StringUtil011.isBlank(text) || StringUtil011.isBlank(pattern)) {
            return null;
        }

        try {
            return LocalDateTime.parse(text.trim(), DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /**
     * Start of the given day, i.e. {@code 00:00:00}.
     *
     * @param date date, may be null
     * @return start of day, or null when the date is null
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    /**
     * Start of today, i.e. {@code 00:00:00}.
     *
     * @return start of the current day
     */
    public static LocalDateTime todayStart() {
        return startOfDay(LocalDate.now());
    }

    /**
     * All patterns owned by this utility.
     *
     * @return immutable list of pattern constants
     */
    public static List<String> patterns() {
        return PATTERNS;
    }
}
