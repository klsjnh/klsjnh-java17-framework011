package com.klsjnh.common.identity;

/*                OperatorContext011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  request-scoped operator holder
 *
 */

/**
 * Request-scoped current-operator holder: the web auth filter sets it at the
 * start of a request and clears it at the end; the table gateway reads it to
 * stamp {@code create_by}/{@code update_by} without threading the operator
 * through every use-case signature.
 */

public final class OperatorContext011 {

    /**
     * Per-thread current operator.
     */
    private static final ThreadLocal<Operator011> CURRENT = new ThreadLocal<>();

    /**
     * Utility: no instances.
     */
    private OperatorContext011() {
    }

    /**
     * Set the current operator for this request.
     *
     * @param operator operator, nullable to clear
     */
    public static void set(Operator011 operator) {
        if (operator == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(operator);
        }
    }

    /**
     * The current operator.
     *
     * @return operator or null
     */
    public static Operator011 get() {
        return CURRENT.get();
    }

    /**
     * Clear the holder (call in a finally block).
     */
    public static void clear() {
        CURRENT.remove();
    }
}
