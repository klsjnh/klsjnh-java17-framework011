package com.klsjnh.common.util;

/*                HttpResponseStream011 record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  minimal http stream response record
 *
 */

import java.io.InputStream;

/**
 * Minimal HTTP stream response carried by {@link HttpUtil011#postJsonStream}:
 * the status code plus the response body as an open stream. Callers MUST check
 * {@link #isSuccess()} before consuming the stream, and MUST close it.
 *
 * @param status HTTP status code
 * @param body   response body stream
 */

public record HttpResponseStream011(int status, InputStream body) {

    /**
     * Whether the status is a 2xx success.
     *
     * @return true when 200-299
     */
    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }
}
