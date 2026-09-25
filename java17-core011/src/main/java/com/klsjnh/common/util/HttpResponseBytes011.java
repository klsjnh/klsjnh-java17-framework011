package com.klsjnh.common.util;

/*                HttpResponseBytes011 record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  minimal http bytes response record
 *
 */

/**
 * Minimal HTTP response carried by the byte-oriented {@link HttpUtil011} calls
 * ({@code getBytes} / {@code postJsonBytes}): the status code plus the response
 * body as bytes. Callers check {@link #isSuccess()} before using the bytes.
 *
 * @param status HTTP status code
 * @param body   response body as bytes, may be empty
 */

public record HttpResponseBytes011(int status, byte[] body) {

    /**
     * Whether the status is a 2xx success.
     *
     * @return true when 200-299
     */
    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }
}
