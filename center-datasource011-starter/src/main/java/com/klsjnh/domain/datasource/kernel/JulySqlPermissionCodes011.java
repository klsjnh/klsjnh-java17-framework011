package com.klsjnh.domain.datasource.kernel;

/*                JulySqlPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july sql query permission codes
 *
 */

/**
 * Permission codes for read-only julySql queries — must match catalog seed.
 */

public final class JulySqlPermissionCodes011 {

    /**
     * Run a read-only paged SELECT.
     */
    public static final String SELECT = "datasource:julySql:select";

    private JulySqlPermissionCodes011() {
    }
}
