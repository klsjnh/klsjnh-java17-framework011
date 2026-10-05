package com.klsjnh.common.util;

/*                SqlGuard011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  read-only guard unit test (incl. server-file access forms)
 *
 */

import com.klsjnh.common.exception.BusinessException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link SqlGuard011}: read-only enforcement including the
 * server-file access forms ({@code INTO OUTFILE} / {@code DUMPFILE} /
 * {@code LOAD_FILE}).
 */

class SqlGuard011Test {

    /**
     * A plain parameterless SELECT passes.
     */
    @Test
    void plainSelectPasses() {
        assertDoesNotThrow(() -> SqlGuard011.assertReadOnly("SELECT id, name FROM july_user WHERE id = ?"));
    }

    /**
     * DML keywords are rejected.
     */
    @Test
    void dmlIsRejected() {
        assertThrows(BusinessException.class, () -> SqlGuard011.assertReadOnly("DELETE FROM july_user"));
    }

    /**
     * Multiple statements are rejected.
     */
    @Test
    void semicolonIsRejected() {
        assertThrows(BusinessException.class, () -> SqlGuard011.assertReadOnly("SELECT 1; DROP TABLE x"));
    }

    /**
     * SELECT ... INTO OUTFILE / DUMPFILE is rejected (server-file write).
     */
    @Test
    void intoOutfileIsRejected() {
        assertThrows(BusinessException.class,
                () -> SqlGuard011.assertReadOnly("SELECT data FROM t INTO OUTFILE '/var/tmp/x'"));

        assertThrows(BusinessException.class,
                () -> SqlGuard011.assertReadOnly("SELECT data FROM t INTO DUMPFILE '/var/tmp/x'"));
    }

    /**
     * LOAD_FILE() is rejected (server-file read).
     */
    @Test
    void loadFileIsRejected() {
        assertThrows(BusinessException.class,
                () -> SqlGuard011.assertReadOnly("SELECT LOAD_FILE('/etc/passwd')"));
    }

    /**
     * A keyword-like identifier stays allowed (whole-word matching).
     */
    @Test
    void keywordLikeIdentifierPasses() {
        assertDoesNotThrow(() -> SqlGuard011.assertReadOnly("SELECT created_at FROM july_user"));
    }
}
