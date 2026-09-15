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
}
