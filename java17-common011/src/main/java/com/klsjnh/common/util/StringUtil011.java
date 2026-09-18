package com.klsjnh.common.util;

/*                StringUtil011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  string util 011 class
 *
 */

/**
 * String helpers for the required / max-length validation pattern. Pure Java
 * shared kernel — usable from the domain layer.
 */

public final class StringUtil011 {

    /**
     * Utility class, no instances.
     */
    private StringUtil011() {
    }

    /**
     * Whether the value is null or blank.
     *
     * @param value value
     * @return true when null or blank
     */
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Whether an optional value is over the max length.
     *
     * @param value value
     * @param max   max length
     * @return true when over the max length
     */
    public static boolean isOver(String value, int max) {
        return value != null && value.length() > max;
    }

    /**
     * Whether a required value is missing (null / blank) or over the max length.
     *
     * @param value value
     * @param max   max length
     * @return true when missing or over the max length
     */
    public static boolean isMissing(String value, int max) {
        return isBlank(value) || value.length() > max;
    }

    /**
     * Compare two nullable strings after trimming, treating null and blank as
     * the same value.
     *
     * @param left  left value
     * @param right right value
     * @return true when both are blank or both hold the same trimmed text
     */
    public static boolean equalsTrimmed(String left, String right) {
        String normalizedLeft = isBlank(left) ? null : left.trim();
        String normalizedRight = isBlank(right) ? null : right.trim();

        return normalizedLeft == null ? normalizedRight == null : normalizedLeft.equals(normalizedRight);
    }

    /**
     * Null-safe string equality (no trimming).
     *
     * @param left  left value
     * @param right right value
     * @return true when both null or both equal
     */
    public static boolean eq(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    /**
     * Normalize a blank optional string to null.
     *
     * @param value raw value
     * @return trimmed value or null when blank
     */
    public static String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    /**
     * Require a value to be within a maximum length, else fail with a readable
     * message. The single home for the "check length then throw" pattern.
     *
     * @param value value, nullable
     * @param label human label used in the message, e.g. {@code driver class}
     * @param max   maximum length
     */
    public static void requireMax(String value, String label, int max) {
        if (isOver(value, max)) {
            throw new IllegalArgumentException(label + " is over " + max);
        }
    }

    /**
     * Require a value to be present (non-blank) and within a maximum length,
     * else fail with a readable message. The single home for the "check present
     * then throw" pattern.
     *
     * @param value value, nullable
     * @param label human label used in the message, e.g. {@code user account}
     * @param max   maximum length
     */
    public static void requirePresent(String value, String label, int max) {
        if (isMissing(value, max)) {
            throw new IllegalArgumentException(label + " is required (max " + max + ")");
        }
    }
}
