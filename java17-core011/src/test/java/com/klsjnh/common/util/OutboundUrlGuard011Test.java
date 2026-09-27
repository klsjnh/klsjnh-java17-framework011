package com.klsjnh.common.util;

/*                OutboundUrlGuard011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  outbound url guard unit tests
 *
 */

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link OutboundUrlGuard011}: scheme and private-target rules.
 */

class OutboundUrlGuard011Test {

    @BeforeEach
    void resetPolicy() {
        OutboundUrlGuard011.configure(OutboundUrlGuard011.Policy.defaults());
    }

    @AfterEach
    void restorePolicy() {
        OutboundUrlGuard011.configure(OutboundUrlGuard011.Policy.defaults());
    }

    /**
     * Loopback targets are rejected under the default policy.
     */
    @Test
    void rejectsLoopbackHttps() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> OutboundUrlGuard011.assertSafe("https://127.0.0.1/hook"));
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("not allowed"));
    }

    /**
     * Cloud metadata link-local address is rejected.
     */
    @Test
    void rejectsLinkLocalMetadata() {
        assertThrows(IllegalArgumentException.class,
                () -> OutboundUrlGuard011.assertSafe("https://169.254.169.254/latest/meta-data"));
    }

    /**
     * Non-http(s) schemes such as file:// are rejected.
     */
    @Test
    void rejectsNonHttpScheme() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> OutboundUrlGuard011.assertSafe("file:///etc/passwd"));
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("scheme"));
    }

    /**
     * Plain http is rejected unless allowHttp is enabled.
     */
    @Test
    void rejectsHttpUnlessAllowed() {
        assertThrows(IllegalArgumentException.class,
                () -> OutboundUrlGuard011.assertSafe("http://1.1.1.1/path"));

        OutboundUrlGuard011.configure(new OutboundUrlGuard011.Policy(true, false, 1024));
        assertDoesNotThrow(() -> OutboundUrlGuard011.assertSafe("http://1.1.1.1/path"));
    }

    /**
     * Public https targets are allowed under the default policy.
     */
    @Test
    void allowsPublicHttps() {
        assertDoesNotThrow(() -> OutboundUrlGuard011.assertSafe("https://1.1.1.1/dns-query"));
    }

    /**
     * allowPrivate opens loopback for local development only.
     */
    @Test
    void allowPrivatePermitsLoopback() {
        OutboundUrlGuard011.configure(new OutboundUrlGuard011.Policy(true, true, 1024));
        assertDoesNotThrow(() -> OutboundUrlGuard011.assertSafe("http://127.0.0.1:8080/hook"));
    }
}
