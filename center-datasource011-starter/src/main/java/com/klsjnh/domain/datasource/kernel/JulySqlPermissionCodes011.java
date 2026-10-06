package com.klsjnh.domain.datasource.kernel;

/*                JulySqlPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.10.05
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july sql query permission codes
 *      2026.10.05  execute code (D3 write-access endpoint)
 *
 */

/**
 * Permission codes for the julySql endpoints — must match catalog seed.
 */

public final class JulySqlPermissionCodes011 {

    /**
     * Run a read-only paged SELECT.
     */
    public static final String SELECT = "datasource:julySql:select";

    /**
     * Run a single authorized statement (DML / DDL) with bound parameters.
     */
    public static final String EXECUTE = "datasource:julySql:execute";

    private JulySqlPermissionCodes011() {
    }
}
