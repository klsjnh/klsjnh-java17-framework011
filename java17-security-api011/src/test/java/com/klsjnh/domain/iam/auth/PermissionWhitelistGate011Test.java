package com.klsjnh.domain.iam.auth;

/*                PermissionWhitelistGate011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  unit tests for permission whitelist gate + status mode
 *
 */

import com.klsjnh.common.enums.FrameworkStatus011;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests: mode resolution from {@link FrameworkStatus011} and production
 * unchecked-write → deny via {@link PermissionWhitelistGate011}.
 */

class PermissionWhitelistGate011Test {

    /**
     * Clear ThreadLocal between cases.
     */
    @AfterEach
    void tearDown() {
        PermissionCheckContext011.clear();
    }

    /**
     * Production status selects whitelist PEP; debug / development stay opt-in.
     */
    @Test
    void statusSelectsPermissionMode() {
        assertTrue(FrameworkStatus011.PRODUCTION.isPermissionWhitelistMode());
        assertTrue(FrameworkStatus011.PRODUCTION.isProduction());
        assertFalse(FrameworkStatus011.DEBUG.isPermissionWhitelistMode());
        assertFalse(FrameworkStatus011.DEVELOPMENT.isPermissionWhitelistMode());
        assertFalse(FrameworkStatus011.fromString("debug").isPermissionWhitelistMode());
        assertTrue(FrameworkStatus011.fromString("production").isPermissionWhitelistMode());
        assertTrue(FrameworkStatus011.fromString("").isPermissionWhitelistMode());
    }

    /**
     * Opt-in mode never denies for missing assertHas mark.
     */
    @Test
    void optInNeverDeniesUnchecked() {
        assertFalse(PermissionWhitelistGate011.shouldDenyUnchecked(false, "POST",
                "/klsjnh/iam/julyUser/v1/insert", false));
    }

    /**
     * Production whitelist denies unchecked mutating protected POST.
     */
    @Test
    void productionDeniesUncheckedWrite() {
        assertTrue(PermissionWhitelistGate011.shouldDenyUnchecked(true, "POST",
                "/klsjnh/iam/julyUser/v1/insert", false));
        assertTrue(PermissionWhitelistGate011.shouldDenyUnchecked(true, "POST",
                "/klsjnh/iam/julyRole/v1/assignMenus", false));
        assertTrue(PermissionWhitelistGate011.shouldDenyUnchecked(true, "DELETE",
                "/klsjnh/system011/julyConfig/v1/logicDelete", false));
    }

    /**
     * Marked assertHas passes the whitelist gate.
     */
    @Test
    void productionAllowsWhenChecked() {
        PermissionCheckContext011.markChecked();
        assertFalse(PermissionWhitelistGate011.shouldDenyUnchecked(true, "POST",
                "/klsjnh/iam/julyUser/v1/insert", PermissionCheckContext011.wasChecked()));
    }

    /**
     * GET and select/get-class path actions stay temporarily opt-in.
     */
    @Test
    void readLikeStaysOptInTemporarily() {
        assertFalse(PermissionWhitelistGate011.requiresCheckedPermission("GET",
                "/klsjnh/iam/julyUser/v1/getById"));
        assertFalse(PermissionWhitelistGate011.requiresCheckedPermission("POST",
                "/klsjnh/iam/julyUser/v1/selectListByPage"));
        assertFalse(PermissionWhitelistGate011.requiresCheckedPermission("POST",
                "/klsjnh/iam/julyUser/v1/logout"));
        assertFalse(PermissionWhitelistGate011.shouldDenyUnchecked(true, "GET",
                "/klsjnh/iam/julyUser/v1/getById", false));
        assertFalse(PermissionWhitelistGate011.shouldDenyUnchecked(true, "POST",
                "/klsjnh/iam/julyUser/v1/selectListByPage", false));
    }

    /**
     * Paths outside /klsjnh/ are out of scope.
     */
    @Test
    void nonProtectedPathOutOfScope() {
        assertFalse(PermissionWhitelistGate011.requiresCheckedPermission("POST", "/actuator/info"));
        assertFalse(PermissionWhitelistGate011.shouldDenyUnchecked(true, "POST", "/other/api", false));
    }
}
