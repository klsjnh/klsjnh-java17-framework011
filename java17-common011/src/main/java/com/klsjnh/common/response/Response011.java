package com.klsjnh.common.response;

/*                Response011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  response 011 class
 *
 */

import com.klsjnh.common.enums.HttpCodeEnum011;
import com.klsjnh.common.vo.IdVo011;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Unified API response envelope returned by every endpoint.
 * <p>
 * The serialized shape is a stable contract, fixed by
 * {@code docs/016.api-contract.md}: {@code statusCode} / {@code message} /
 * {@code errorMessage} / {@code timestamp} / {@code traceId} / {@code data}.
 * Every key is camelCase and MUST NOT be renamed, dropped or reordered.
 * </p>
 * <p>
 * {@code timestamp} is the server time in epoch milliseconds, taken when the
 * envelope is created. {@code traceId} is filled by the global response advice
 * from the MDC value injected by {@code GlobalAuthFilter}. {@code errorMessage}
 * carries internal detail ONLY in debug mode and stays {@code ""} in every
 * other environment, so internal state never leaks to callers.
 * </p>
 *
 * @param <T> payload type carried by {@code data}
 */

@Getter
@Setter
public class Response011<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Business status code, kept in sync with the HTTP status code. Values come
     * from {@link HttpCodeEnum011}; 200 = success.
     */
    private Integer statusCode = HttpCodeEnum011.SUCCESS.getCode();

    /**
     * Human readable message: {@code "<action> success"} on success, a safe
     * hint on failure. Never exposes internal detail.
     */
    private String message = HttpCodeEnum011.SUCCESS.getMsg();

    /**
     * Detailed failure information, filled ONLY in debug mode; empty string in
     * every other environment.
     */
    private String errorMessage = "";

    /**
     * Server timestamp in epoch milliseconds.
     */
    private Long timestamp = System.currentTimeMillis();

    /**
     * Trace id injected from MDC by {@code GlobalAuthFilter}; null when
     * tracing is not available.
     */
    private String traceId;

    /**
     * Business payload; null for write operations that return no data.
     */
    private T data;

    /**
     * Create a success envelope without data.
     *
     * @param <T> payload type
     * @return envelope with statusCode 200 and message "success"
     */
    public static <T> Response011<T> create() {
        return new Response011<>();
    }

    /**
     * Create a success envelope carrying data.
     *
     * @param data response payload
     * @param <T>  payload type
     * @return envelope with statusCode 200 and message "success"
     */
    public static <T> Response011<T> success(T data) {
        Response011<T> body = new Response011<>();
        body.setData(data);
        return body;
    }

    /**
     * Create a success envelope whose message names the executed action.
     *
     * @param action operation name, e.g. "insert" or "logic delete"
     * @param data   response payload, may be null
     * @param <T>    payload type
     * @return envelope whose message is {@code "<action> success"}
     */
    public static <T> Response011<T> success(String action, T data) {
        Response011<T> body = new Response011<>();
        body.setMessage(action + " success");
        body.setData(data);
        return body;
    }

    /**
     * Create a success envelope with an id payload.
     *
     * @param action operation name, e.g. "insert" or "update"
     * @param id     primary key of the affected row
     * @return envelope whose data is {@code {"id": "<id>"}}
     */
    public static Response011<IdVo011> successId(String action, String id) {
        IdVo011 data = new IdVo011();
        data.setId(id);
        return success(action, data);
    }

    /**
     * Create a server error envelope.
     *
     * @param <T> payload type
     * @return envelope with statusCode 500
     */
    public static <T> Response011<T> error() {
        return of(HttpCodeEnum011.ERROR);
    }

    /**
     * Create an unauthorized envelope.
     *
     * @param <T> payload type
     * @return envelope with statusCode 401
     */
    public static <T> Response011<T> unauthorized() {
        return of(HttpCodeEnum011.UNAUTHORIZED);
    }

    /**
     * Create an envelope from a status code constant with a custom safe hint.
     *
     * @param codeEnum status code constant
     * @param message  safe human readable hint; null falls back to the default
     *                 message of the constant
     * @param <T>      payload type
     * @return envelope carrying the code and the given message
     */
    public static <T> Response011<T> of(HttpCodeEnum011 codeEnum, String message) {
        Response011<T> body = new Response011<>();
        body.setStatusCode(codeEnum.getCode());
        body.setMessage(message != null ? message : codeEnum.getMsg());
        return body;
    }

    /**
     * Create an envelope from a raw status code with a custom safe hint.
     *
     * @param statusCode contract status code
     * @param message    safe human readable hint; null falls back to an empty
     *                   message
     * @param <T>        payload type
     * @return envelope carrying the code and the given message
     */
    public static <T> Response011<T> of(Integer statusCode, String message) {
        Response011<T> body = new Response011<>();
        body.setStatusCode(statusCode);
        body.setMessage(message == null ? "" : message);

        return body;
    }

    /**
     * Create an envelope from a status code constant.
     *
     * @param codeEnum status code constant
     * @param <T>      payload type
     * @return envelope carrying the code and its default message
     */
    private static <T> Response011<T> of(HttpCodeEnum011 codeEnum) {
        return of(codeEnum, codeEnum.getMsg());
    }
}
