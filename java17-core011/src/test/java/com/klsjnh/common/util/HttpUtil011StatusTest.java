package com.klsjnh.common.util;

/*                HttpUtil011StatusTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  upstream status classification tests
 *
 */

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link HttpUtil011#failureHint(int)} and
 * {@link HttpUtil011#describeStatus(int)}.
 */

class HttpUtil011StatusTest {

    @Test
    void classifiesAuthStatuses() {
        assertEquals("authentication failed", HttpUtil011.failureHint(401));
        assertEquals("authentication failed", HttpUtil011.failureHint(403));
    }

    @Test
    /**
     * Classifies payload-size and throttling statuses.
     */
    void classifiesSizeAndThrottling() {
        assertEquals("request payload too large", HttpUtil011.failureHint(413));
        assertEquals("rate limited", HttpUtil011.failureHint(429));
    }

    @Test
    /**
     * Classifies 5xx and other non-2xx statuses.
     */
    void classifiesUpstreamAndOtherRejections() {
        assertEquals("upstream error", HttpUtil011.failureHint(500));
        assertEquals("upstream error", HttpUtil011.failureHint(503));
        assertEquals("request rejected", HttpUtil011.failureHint(400));
        assertEquals("request rejected", HttpUtil011.failureHint(404));
    }

    @Test
    /**
     * Formats status and hint together.
     */
    void describesStatusWithHint() {
        assertEquals("HTTP 413 (request payload too large)", HttpUtil011.describeStatus(413));
    }
}
