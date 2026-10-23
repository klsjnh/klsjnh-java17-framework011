package com.klsjnh.common.enums;

/*                FrameworkStatus011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.23
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.23  tolerant status prefixes (prod / dev* / debug*)
 *
 */

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the tolerant {@code krt.status} parser.
 */

class FrameworkStatus011Test {

    /**
     * Blank / null fails closed to production.
     */
    @Test
    void blankFallsBackToProduction() {
        assertEquals(FrameworkStatus011.PRODUCTION, FrameworkStatus011.fromString(null));
        assertEquals(FrameworkStatus011.PRODUCTION, FrameworkStatus011.fromString(""));
        assertEquals(FrameworkStatus011.PRODUCTION, FrameworkStatus011.fromString("   "));
    }

    /**
     * Production accepts the canonical name and the {@code prod} prefix.
     */
    @Test
    void productionNamesResolveToProduction() {
        assertEquals(FrameworkStatus011.PRODUCTION, FrameworkStatus011.fromString("production"));
        assertEquals(FrameworkStatus011.PRODUCTION, FrameworkStatus011.fromString("PROD"));
        assertEquals(FrameworkStatus011.PRODUCTION, FrameworkStatus011.fromString(" prod "));
    }

    /**
     * Any {@code debug*} value resolves to debug.
     */
    @Test
    void debugPrefixesResolveToDebug() {
        assertEquals(FrameworkStatus011.DEBUG, FrameworkStatus011.fromString("debug"));
        assertEquals(FrameworkStatus011.DEBUG, FrameworkStatus011.fromString("debug011"));
        assertEquals(FrameworkStatus011.DEBUG, FrameworkStatus011.fromString("DEBUG-local"));
    }

    /**
     * Any {@code dev*} / {@code development} value resolves to development.
     */
    @Test
    void developmentPrefixesResolveToDevelopment() {
        assertEquals(FrameworkStatus011.DEVELOPMENT, FrameworkStatus011.fromString("development"));
        assertEquals(FrameworkStatus011.DEVELOPMENT, FrameworkStatus011.fromString("dev"));
        assertEquals(FrameworkStatus011.DEVELOPMENT, FrameworkStatus011.fromString("dev011"));
        assertEquals(FrameworkStatus011.DEVELOPMENT, FrameworkStatus011.fromString("DEV-local"));
    }

    /**
     * Unknown values are rejected at startup instead of silently selecting a
     * mode.
     */
    @Test
    void unknownValueIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> FrameworkStatus011.fromString("staging"));
    }
}
