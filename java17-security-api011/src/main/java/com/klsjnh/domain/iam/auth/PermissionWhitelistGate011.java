package com.klsjnh.domain.iam.auth;

/*                PermissionWhitelistGate011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  decide which requests need assertHas under whitelist mode
 *
 */

/**
 * Pure decision helper for production permission whitelist enforcement.
 * <p>
 * When {@code krt.status=production}, mutating requests under {@code /klsjnh/**}
 * must have called {@code assertHas} (or equivalent) during the request;
 * otherwise the filter answers 403. Safe HTTP methods and temporary
 * select/get-class path actions stay opt-in so a single cutover does not
 * fail-close every list endpoint that uses POST.
 * </p>
 */

public final class PermissionWhitelistGate011 {

    /**
     * Protected API prefix (management + business under the framework).
     */
    public static final String PROTECTED_PREFIX = "/klsjnh/";

    /**
     * Not instantiable.
     */
    private PermissionWhitelistGate011() {
    }

    /**
     * Whether an unchecked request must be denied under whitelist mode.
     *
     * @param whitelistMode current mode from {@code krt.status}
     * @param method        HTTP method
     * @param normalizedUri normalized request path
     * @param wasChecked    whether assertHas marked the request
     * @return true when the filter should answer 403
     */
    public static boolean shouldDenyUnchecked(boolean whitelistMode, String method, String normalizedUri,
            boolean wasChecked) {
        return whitelistMode && requiresCheckedPermission(method, normalizedUri) && !wasChecked;
    }

    /**
     * Whether this request is in scope for the production whitelist (must have
     * been permission-checked). Login / doc / open / health are excluded by the
     * auth filter {@code shouldNotFilter} and never reach this gate.
     *
     * @param method        HTTP method
     * @param normalizedUri normalized request path
     * @return true when assertHas (or equivalent) is required
     */
    public static boolean requiresCheckedPermission(String method, String normalizedUri) {
        if (method == null || normalizedUri == null) {
            return false;
        }

        if (!normalizedUri.startsWith(PROTECTED_PREFIX)) {
            return false;
        }

        if (isSafeHttpMethod(method)) {
            return false;
        }

        return !isTemporarilyOptInAction(normalizedUri);
    }

    /**
     * Safe / idempotent read HTTP methods — stay opt-in even in production.
     *
     * @param method HTTP method
     * @return true for GET / HEAD / OPTIONS / TRACE
     */
    static boolean isSafeHttpMethod(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method) || "TRACE".equalsIgnoreCase(method);
    }

    /**
     * Temporary production exemption for select/get-class actions (often POST
     * body queries). Mutating actions (insert / update / delete / assign / …)
     * are NOT exempt. Documented as transitional; prefer hanging assertHas on
     * reads too before tightening.
     *
     * @param normalizedUri normalized request path
     * @return true when the last path segment is treated as read-like
     */
    static boolean isTemporarilyOptInAction(String normalizedUri) {
        String action = lastPathSegment(normalizedUri);

        if (action.isEmpty()) {
            return false;
        }

        return action.startsWith("select") || action.startsWith("get") || action.startsWith("stat")
                || action.startsWith("download") || "logout".equals(action);
    }

    /**
     * Last non-empty path segment of a URI.
     *
     * @param uri normalized path
     * @return last segment, or empty
     */
    static String lastPathSegment(String uri) {
        if (uri == null || uri.isEmpty()) {
            return "";
        }

        int end = uri.length();

        while (end > 0 && uri.charAt(end - 1) == '/') {
            end--;
        }

        if (end == 0) {
            return "";
        }

        int slash = uri.lastIndexOf('/', end - 1);

        return slash < 0 ? uri.substring(0, end) : uri.substring(slash + 1, end);
    }
}
