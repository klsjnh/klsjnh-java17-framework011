package com.klsjnh.common.enums;

/*                HttpCodeEnum011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  http code enum 011 class
 *
 */

/**
 * HTTP-style status codes carried by the unified API response envelope.
 * <p>
 * The {@code code} of every constant equals the HTTP status code the endpoint
 * returns, so the transport status and the body {@code statusCode} always stay
 * in sync. Callers MUST NOT add cases without extending the API contract
 * documentation first.
 * </p>
 */

public enum HttpCodeEnum011 {

    /** Request succeeded. */
    SUCCESS(200, "success"),

    /** Malformed request or validation failure. */
    BAD_REQUEST(400, "bad request"),

    /** Missing, malformed, or expired token. */
    UNAUTHORIZED(401, "unauthorized"),

    /** Authenticated but not allowed. */
    FORBIDDEN(403, "forbidden"),

    /** Resource or endpoint does not exist. */
    NOT_FOUND(404, "not found"),

    /** Unexpected server-side failure. */
    ERROR(500, "internal server error");

    /**
     * Business status code, identical to the HTTP status code.
     */
    private final Integer code;

    /**
     * Default human readable message bound to the code.
     */
    private final String msg;

    /**
     * Create a status code constant.
     *
     * @param code business status code, equals the HTTP status code
     * @param msg  default message carried by the envelope
     */
    HttpCodeEnum011(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * Get the business status code.
     *
     * @return status code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * Get the default message.
     *
     * @return message
     */
    public String getMsg() {
        return msg;
    }
}
