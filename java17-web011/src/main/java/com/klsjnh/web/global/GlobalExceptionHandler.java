package com.klsjnh.web.global;

/*                GlobalExceptionHandler class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  global exception handler class
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.enums.HttpCodeEnum011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.response.Response011;

import com.klsjnh.domain.iam.RuntimeStatusPort;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler: maps every exception onto the unified response
 * envelope with the HTTP status kept in sync with the envelope statusCode.
 * <p>
 * errorMessage carries internal detail ONLY in debug mode and stays empty in
 * every other environment, so stack traces and internal state never leak to
 * callers. Unexpected exceptions are logged with the full stack trace — the
 * stack itself stays server-side.
 * </p>
 */

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Runtime status port (debug gate for errorMessage).
     */
    private final RuntimeStatusPort runtimeStatusPort;

    /**
     * Create the handler.
     *
     * @param runtimeStatusPort runtime status port
     */
    public GlobalExceptionHandler(RuntimeStatusPort runtimeStatusPort) {
        this.runtimeStatusPort = runtimeStatusPort;
    }

    /**
     * Map a business exception onto the envelope: the contract status code
     * becomes both the HTTP status and the envelope statusCode.
     *
     * @param ex business exception
     * @return error envelope
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Response011<Void>> handleBusiness(BusinessException ex) {
        Response011<Void> body = Response011.of(ex.getCode(), ex.getMessage());

        return ResponseEntity.status(ex.getCode()).body(body);
    }

    /**
     * Map a malformed request body onto a bad request envelope.
     *
     * @param ex unreadable body exception
     * @return error envelope
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response011<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        Response011<Void> body = Response011.of(HttpCodeEnum011.BAD_REQUEST, "malformed request body");

        if (runtimeStatusPort.isDebug()) {
            body.setErrorMessage(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Fallback for unexpected exceptions: generic envelope, full detail only in
     * debug mode, the stack trace stays in the server log.
     *
     * @param ex unexpected exception
     * @return error envelope
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response011<Void>> handleUnexpected(Exception ex) {
        logger.error("unhandled exception ...", ex);

        Response011<Void> body = Response011.of(HttpCodeEnum011.ERROR, "internal server error");

        if (runtimeStatusPort.isDebug()) {
            body.setErrorMessage(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }

        return ResponseEntity.internalServerError().body(body);
    }
}
