package com.klsjnh.observability;

/*                TraceIdMdcFilter011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  trace id mdc filter (runs before the auth filter so
 *                  whitelisted paths get a trace id too)
 *
 */

import org.slf4j.MDC;

import com.klsjnh.common.constant.FrameConst011;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

/**
 * Puts the per-request trace id into the MDC before every other filter, so
 * whitelisted paths (docs, actuator, open API) also carry it in logs. The
 * auth filter reuses an existing MDC value instead of generating its own.
 */

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdMdcFilter011 extends OncePerRequestFilter {

    /**
     * Resolve or generate the trace id, expose it in the MDC and the request,
     * and always clear the MDC afterwards.
     *
     * @param request     http request
     * @param response    http response
     * @param filterChain remaining filter chain
     * @throws ServletException servlet failure
     * @throws IOException      io failure
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        MDC.put(FrameConst011.TRACE_ID, traceId);
        request.setAttribute(FrameConst011.TRACE_ID, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(FrameConst011.TRACE_ID);
        }
    }

    /**
     * Resolve the per-request trace id: the incoming {@code X-Trace-Id} header
     * when present, an existing MDC value next, otherwise a fresh UUID.
     *
     * @param request http request
     * @return trace id, never blank
     */
    private String resolveTraceId(HttpServletRequest request) {
        String header = request.getHeader("X-Trace-Id");

        if (header != null && !header.isBlank()) {
            return header.trim();
        }

        String existing = MDC.get(FrameConst011.TRACE_ID);

        if (existing != null && !existing.isBlank()) {
            return existing;
        }

        return UUID.randomUUID().toString().replace("-", "");
    }
}
