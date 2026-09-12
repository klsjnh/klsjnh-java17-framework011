package com.klsjnh.web.global;

/*                GlobalAuthFilter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  global auth filter class
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.constant.AuthAttribute011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.domain.iam.AuthTokenPort;
import com.klsjnh.domain.iam.RuntimeStatusPort;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT authentication filter. Verifies the {@code Authorization: Bearer} token
 * and records the authenticated operator id as a request attribute for the
 * audit auto-fill.
 * <p>
 * In debug mode a valid token is still parsed when present (so the audit works
 * locally) but a missing token never rejects. In every other mode a request
 * without a valid token is answered with a 401 envelope written here, because
 * filter failures happen before the controller advice can handle them.
 * </p>
 */

@Component
public class GlobalAuthFilter extends OncePerRequestFilter {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalAuthFilter.class);

    /**
     * Authorization scheme prefix.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Paths that never require a token.
     */
    private static final List<String> WHITELIST_PATHS = List.of(
            "/klsjnh/system011/julyUser/v1/login",
            "/klsjnh/system011/julyUser/v1/loginByUserName");

    /**
     * Path prefixes that never require a token (API docs, error page).
     */
    private static final List<String> WHITELIST_PREFIXES = List.of(
            "/doc.html", "/webjars/", "/v3/api-docs", "/swagger-ui", "/swagger-resources", "/favicon.ico", "/error");

    /**
     * Token port (verify only; issuing stays in the login flow).
     */
    private final AuthTokenPort authTokenPort;

    /**
     * Runtime status port (debug gate).
     */
    private final RuntimeStatusPort runtimeStatusPort;

    /**
     * JSON mapper for the 401 envelope.
     */
    private final ObjectMapper objectMapper;

    /**
     * Create the filter.
     *
     * @param authTokenPort     token port
     * @param runtimeStatusPort runtime status port
     * @param objectMapper      json mapper
     */
    public GlobalAuthFilter(AuthTokenPort authTokenPort, RuntimeStatusPort runtimeStatusPort,
            ObjectMapper objectMapper) {
        this.authTokenPort = authTokenPort;
        this.runtimeStatusPort = runtimeStatusPort;
        this.objectMapper = objectMapper;
    }

    /**
     * Skip preflight requests and the whitelisted login / docs / error paths.
     *
     * @param request http request
     * @return true when the filter should not run
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();

        return WHITELIST_PATHS.contains(uri) || WHITELIST_PREFIXES.stream().anyMatch(uri::startsWith);
    }

    /**
     * Verify the bearer token, expose the operator id, then gate on the runtime
     * status.
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
        String operatorId = resolveOperatorId(request);

        if (operatorId != null) {
            request.setAttribute(AuthAttribute011.OPERATOR_ID, operatorId);
        }

        if (operatorId == null && !runtimeStatusPort.isDebug()) {
            writeUnauthorized(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Resolve the operator id from a bearer token, best effort.
     *
     * @param request http request
     * @return operator id, or null when the header is missing / invalid
     */
    private String resolveOperatorId(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        return token.isEmpty() ? null : authTokenPort.verifyAndGetId(token);
    }

    /**
     * Write the 401 envelope directly (controller advice cannot see this).
     *
     * @param request  http request
     * @param response http response
     * @throws IOException write failure
     */
    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("auth filter rejected request {} ...", request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), Response011.unauthorized());
    }
}
