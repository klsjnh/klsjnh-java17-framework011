package com.klsjnh.common.constant;

/*                FrameConst011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  frame const 011 class
 *      2026.09.13  renamed from AuthAttribute011 as the framework constants home
 *
 */

/**
 * Framework-wide cross-layer constants (the single home for shared
 * framework-level constants such as request attribute keys).
 *
 * <p><b>Admission rules (016.coding-standards §7) — a constant joins here ONLY when all of the
 * following hold:</b></p>
 * <ul>
 *   <li>consumed by at least two layers/modules (cross-layer shared);</li>
 *   <li>it is NOT a message text — message wording belongs to the funcName
 *       pattern and BusinessException static factories (016.coding-standards §7);</li>
 *   <li>it is NOT a status code — status codes belong to HttpCodeEnum011 and
 *       the docs/013.api-contract contract;</li>
 *   <li>it is NOT a column definition — column widths belong to the DDL
 *       template (docs/sql).</li>
 * </ul>
 *
 * <p>This class must never grow into a grab-bag of everything (the old KC
 * anti-pattern): each candidate is owned by a more specific home first, and
 * only truly homeless cross-layer constants land here.</p>
 */

public final class FrameConst011 {

    /**
     * Servlet request attribute holding the authenticated operator user id,
     * set by GlobalAuthFilter (web) and read by the audit fill handler
     * (infrastructure) — the classic cross-layer case.
     */
    public static final String OPERATOR_ID = "krt.operatorId";

    /**
     * Servlet request attribute holding the authenticated operator login
     * account, set by GlobalAuthFilter (web) alongside {@link #OPERATOR_ID} so
     * the audit rows carry a real operator instead of an empty account.
     */
    public static final String OPERATOR_ACCOUNT = "krt.operatorAccount";

    /**
     * Servlet request attribute holding the per-request trace id, set by
     * GlobalAuthFilter (web) and read by the response advice to fill the
     * envelope {@code traceId} — the same cross-layer case as the operator id.
     */
    public static final String TRACE_ID = "krt.traceId";

    /**
     * Constant holder, no instances.
     */
    private FrameConst011() {
    }
}
