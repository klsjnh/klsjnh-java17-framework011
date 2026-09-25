package com.klsjnh.web.global;

/*                TraceIdResponseAdvice class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  fill envelope traceId from MDC
 *
 */

import org.slf4j.MDC;

import com.klsjnh.common.constant.FrameConst011;
import com.klsjnh.common.response.Response011;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Fills the envelope {@code traceId} from the MDC value set by
 * {@code GlobalAuthFilter} (per request), so every {@link Response011} —
 * success or handled failure — carries the same trace id. Non-envelope bodies
 * (file streams, SSE) pass through untouched.
 */

@RestControllerAdvice
public class TraceIdResponseAdvice implements ResponseBodyAdvice<Object> {

    /**
     * Apply to every body (the type check below keeps it a no-op for
     * non-envelopes).
     *
     * @param returnType    controller return type
     * @param converterType selected converter type
     * @return true
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * Set the trace id on an envelope body when the MDC carries one.
     *
     * @param body                  response body
     * @param returnType            controller return type
     * @param selectedContentType   selected content type
     * @param selectedConverterType selected converter type
     * @param request               server request
     * @param response              server response
     * @return the body, unchanged
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof Response011<?> envelope) {
            String traceId = MDC.get(FrameConst011.TRACE_ID);
            if (traceId != null) {
                envelope.setTraceId(traceId);
            }
        }

        return body;
    }
}
