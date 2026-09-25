package com.klsjnh.common.util;

/*                HttpResponse011 record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  minimal http response record
 *
 */

/**
 * Minimal HTTP response carried by {@link HttpUtil011}: the status code plus the
 * response body as text.
 *
 * @param status HTTP status code
 * @param body   response body as text, may be blank
 */

public record HttpResponse011(int status, String body) {

    /**
     * Whether the status is a 2xx success.
     *
     * @return true when 200-299
     */
    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }
}
