package com.klsjnh.common.exception;

/*                BusinessException class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  business exception class
 *
 */

import com.klsjnh.common.enums.HttpCodeEnum011;

/**
 * Business exception carrying a contract status code and a client-safe message.
 * <p>
 * The global exception handler maps {@link #getCode()} onto the response
 * envelope (see docs/016.api-contract.md). The message is a safe hint and
 * never carries internal detail.
 * </p>
 */

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Contract status code from HttpCodeEnum011.
     */
    private final Integer code;

    /**
     * Create the exception.
     *
     * @param code    contract status code
     * @param message client-safe message
     */
    public BusinessException(HttpCodeEnum011 code, String message) {
        super(message);
        this.code = code.getCode();
    }

    /**
     * Build a 400 business exception.
     *
     * @param message client-safe message
     * @return exception instance
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(HttpCodeEnum011.BAD_REQUEST, message);
    }

    /**
     * Build a 401 business exception.
     *
     * @param message client-safe message
     * @return exception instance
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(HttpCodeEnum011.UNAUTHORIZED, message);
    }

    /**
     * Build a 403 business exception.
     *
     * @param message client-safe message
     * @return exception instance
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(HttpCodeEnum011.FORBIDDEN, message);
    }

    /**
     * Build a 404 business exception.
     *
     * @param message client-safe message
     * @return exception instance
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(HttpCodeEnum011.NOT_FOUND, message);
    }

    /**
     * Get the contract status code.
     *
     * @return status code
     */
    public Integer getCode() {
        return code;
    }
}
