package com.klsjnh.domain.iam.auth;

/*                PermissionCheckContext011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  per-request assertHas mark for production whitelist PEP
 *
 */

/**
 * Per-request ThreadLocal mark: whether {@link AuthorizationPort#assertHas}
 * ran during the current request. Used by the production whitelist gate so an
 * unchecked mutating call on a protected path can be rejected with 403.
 * <p>
 * The web filter clears the mark at request start and in {@code finally}.
 * Callers outside HTTP must clear after use if they share a pooled thread.
 * </p>
 */

public final class PermissionCheckContext011 {

    /**
     * True when assertHas has been invoked on this thread for the current
     * request.
     */
    private static final ThreadLocal<Boolean> CHECKED = new ThreadLocal<>();

    /**
     * Not instantiable.
     */
    private PermissionCheckContext011() {
    }

    /**
     * Mark that a permission check occurred on this thread.
     */
    public static void markChecked() {
        CHECKED.set(Boolean.TRUE);
    }

    /**
     * Whether a permission check occurred on this thread.
     *
     * @return true when {@link #markChecked()} was called
     */
    public static boolean wasChecked() {
        return Boolean.TRUE.equals(CHECKED.get());
    }

    /**
     * Clear the mark (call from the request filter finally / start).
     */
    public static void clear() {
        CHECKED.remove();
    }
}
