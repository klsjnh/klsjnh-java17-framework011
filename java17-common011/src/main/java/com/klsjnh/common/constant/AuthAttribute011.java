package com.klsjnh.common.constant;

/*                AuthAttribute011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  auth attribute 011 class
 *
 */

/**
 * Request-scoped attribute keys shared by the auth filter (web layer) and the
 * audit handler (infrastructure layer), so neither module depends on the other.
 */

public final class AuthAttribute011 {

    /**
     * Servlet request attribute holding the authenticated operator user id.
     */
    public static final String OPERATOR_ID = "krt.operatorId";

    /**
     * Constant holder, no instances.
     */
    private AuthAttribute011() {
    }
}
