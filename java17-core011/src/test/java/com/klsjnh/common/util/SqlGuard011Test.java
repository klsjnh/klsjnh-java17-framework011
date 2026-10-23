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
        assertDoesNotThrow(() -> SqlGuard011.assertReadOnly("SELECT id, name FROM demo_table WHERE id = ?"));
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
     * Comment-separated server-file forms are rejected too: MySQL accepts a
     * comment in place of whitespace, so {@code INTO/**&#47;OUTFILE} and {@code
     * LOAD_FILE/**&#47;(...)} are the same statements.
     */
    @Test
    void commentSeparatedFileFormsAreRejected() {
        assertThrows(BusinessException.class,
                () -> SqlGuard011.assertReadOnly("SELECT data FROM t INTO/**/OUTFILE '/var/tmp/x'"));

        assertThrows(BusinessException.class,
                () -> SqlGuard011.assertReadOnly("SELECT LOAD_FILE/**/('/etc/passwd')"));

        assertThrows(BusinessException.class,
                () -> SqlGuard011.assertExecutable("UPDATE t SET a = 1 INTO/*x*/OUTFILE '/var/tmp/x'"));

        assertThrows(BusinessException.class,
                () -> SqlGuard011.assertExecutable("UPDATE t SET a = 1 INTO--c\nOUTFILE '/var/tmp/x'"));
    }

    /**
     * Cross-database server-file / host-command words are rejected on both the
     * read and the execute path.
     */
    @Test
    void crossDbServerAccessIsRejected() {
        assertThrows(BusinessException.class,
                () -> SqlGuard011.assertReadOnly("SELECT pg_read_file('/etc/passwd')"));

        assertThrows(BusinessException.class, () -> SqlGuard011.assertExecutable("EXEC xp_cmdshell 'dir'"));

        assertThrows(BusinessException.class,
                () -> SqlGuard011.assertExecutable("INSERT INTO t SELECT * FROM OPENROWSET('x', 'y', 'z')"));

        assertThrows(BusinessException.class, () -> SqlGuard011.assertExecutable("COPY t TO '/var/tmp/x'"));
    }

    /**
     * Plain DML / DDL still passes the execute guard.
     */
    @Test
    void plainExecutePasses() {
        assertDoesNotThrow(() -> SqlGuard011.assertExecutable("UPDATE demo_table SET name = 'n' WHERE id = 1"));

        assertDoesNotThrow(() -> SqlGuard011.assertExecutable("CREATE INDEX idx_name ON demo_table (name)"));

        assertDoesNotThrow(() -> SqlGuard011.assertExecutable("insert into t (id) values (1)"));
    }

    /**
     * A keyword-like identifier stays allowed (whole-word matching).
     */
    @Test
    void keywordLikeIdentifierPasses() {
        assertDoesNotThrow(() -> SqlGuard011.assertReadOnly("SELECT created_at FROM demo_table"));
    }

    /**
     * Slow / lock functions are rejected.
     */
    @Test
    void timeAndLockFunctionsAreRejected() {
        assertThrows(BusinessException.class, () -> SqlGuard011.assertReadOnly("SELECT SLEEP(10)"));
        assertThrows(BusinessException.class, () -> SqlGuard011.assertReadOnly("SELECT BENCHMARK(1000000, MD5('x'))"));
        assertThrows(BusinessException.class, () -> SqlGuard011.assertReadOnly("SELECT pg_sleep(10)"));
    }

    /**
     * Framework credential tables are rejected on both paths.
     */
    @Test
    void credentialTablesAreRejected() {
        assertThrows(BusinessException.class,
                () -> SqlGuard011.assertReadOnly("SELECT * FROM july_user"));
        assertThrows(BusinessException.class,
                () -> SqlGuard011.assertExecutable("UPDATE july_ai_model_provider_api SET api_key = 'x'"));
    }
}
