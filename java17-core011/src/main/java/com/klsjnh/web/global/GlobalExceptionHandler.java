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

import com.klsjnh.domain.iam.auth.RuntimeStatusPort;

import io.swagger.v3.oas.annotations.Hidden;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

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
     * Map {@code @Valid} / {@code @Validated} body failures onto a bad request
     * envelope. The first field error becomes {@code message}; full detail
     * stays in {@code errorMessage} only in debug mode.
     *
     * @param ex method argument not valid
     * @return error envelope
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response011<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = firstFieldErrorMessage(ex);
        Response011<Void> body = Response011.of(HttpCodeEnum011.BAD_REQUEST, message);

        if (runtimeStatusPort.isDebug()) {
            body.setErrorMessage(ex.getClass().getSimpleName() + ": " + message);
        }

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Map constraint violations (e.g. {@code @Validated} on params) onto a bad
     * request envelope.
     *
     * @param ex constraint violation
     * @return error envelope
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response011<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = firstConstraintMessage(ex);
        Response011<Void> body = Response011.of(HttpCodeEnum011.BAD_REQUEST, message);

        if (runtimeStatusPort.isDebug()) {
            body.setErrorMessage(ex.getClass().getSimpleName() + ": " + message);
        }

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * First field error message from a {@link MethodArgumentNotValidException}.
     *
     * @param ex exception
     * @return message
     */
    private static String firstFieldErrorMessage(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();

        if (fieldError == null) {
            return "validation failed";
        }

        String defaultMessage = fieldError.getDefaultMessage();

        if (defaultMessage == null || defaultMessage.isBlank()) {
            return fieldError.getField() + " is invalid";
        }

        return defaultMessage;
    }

    /**
     * First constraint violation message.
     *
     * @param ex exception
     * @return message
     */
    private static String firstConstraintMessage(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .filter(msg -> msg != null && !msg.isBlank())
                .findFirst()
                .orElse("validation failed");
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
     * Map a missing required request parameter onto a bad request envelope
     * (empty-param probes must not surface as 500).
     *
     * @param ex missing servlet request parameter
     * @return error envelope
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Response011<Void>> handleMissingParameter(MissingServletRequestParameterException ex) {
        String message = ex.getParameterName() + " is required";
        Response011<Void> body = Response011.of(HttpCodeEnum011.BAD_REQUEST, message);

        if (runtimeStatusPort.isDebug()) {
            body.setErrorMessage(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Map multipart parse / missing-part failures onto a bad request envelope.
     *
     * @param ex multipart exception
     * @return error envelope
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Response011<Void>> handleMultipart(MultipartException ex) {
        Response011<Void> body = Response011.of(HttpCodeEnum011.BAD_REQUEST, "multipart request is invalid");

        if (runtimeStatusPort.isDebug()) {
            body.setErrorMessage(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Map a missing static resource (e.g. favicon.ico) onto a not found
     * envelope: it is a client path error, not a server failure, so it never
     * hits the error log.
     *
     * @param ex no resource found exception
     * @return not found envelope
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Response011<Void>> handleNoResource(NoResourceFoundException ex) {
        Response011<Void> body = Response011.of(HttpCodeEnum011.NOT_FOUND, "resource not found");

        return ResponseEntity.status(HttpCodeEnum011.NOT_FOUND.getCode()).body(body);
    }

    /**
     * Map a database unique-key violation onto a bad request envelope: the
     * friendly pre-check only sees alive rows, so a logic-deleted duplicate
     * surfaces here — a 400 is the honest answer, not a 500.
     *
     * @param ex duplicate key exception
     * @return error envelope
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Response011<Void>> handleDuplicateKey(DuplicateKeyException ex) {
        Response011<Void> body = Response011.of(HttpCodeEnum011.BAD_REQUEST, "duplicate key");

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
