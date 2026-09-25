package com.klsjnh.web.global;

/*                GlobalAuthFilter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  global auth filter class
 *      2026.09.26  production permission whitelist (unchecked write → 403)
 *      2026.09.26  merge krt.web.auth-whitelist-paths / prefixes
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.klsjnh.common.constant.FrameConst011;
import com.klsjnh.common.enums.HttpCodeEnum011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.domain.iam.auth.AuthTokenPort;
import com.klsjnh.domain.iam.auth.AuthTokenPort.OperatorIdentity;
import com.klsjnh.domain.iam.auth.PermissionCheckContext011;
import com.klsjnh.domain.iam.auth.PermissionWhitelistGate011;
import com.klsjnh.domain.iam.auth.RuntimeStatusPort;

import com.klsjnh.infrastructure.config.KrtSecurityConfig011;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * JWT authentication filter. Verifies the {@code Authorization: Bearer} token
 * and records the authenticated operator identity (id + account) as request
 * attributes for the audit fill.
 * <p>
 * In debug mode a valid token is still parsed when present (so the audit works
 * locally) but a missing token never rejects. In every other mode a request
 * without a valid token is answered with a 401 envelope written here, because
 * filter failures happen before the controller advice can handle them.
 * </p>
 * <p>
 * When {@code krt.status=production}, after the chain returns this filter also
 * enforces the permission whitelist: a mutating request under {@code /klsjnh/**}
 * that never called {@code assertHas} (no {@link PermissionCheckContext011}
 * mark) is answered with 403. Select/get-class path actions stay temporarily
 * exempt from this gate; debug / development never apply it (and
 * {@code assertHas} itself is a no-op there).
 * </p>
 * <p>
 * Built-in JWT whitelist (login / docs / open / health) can be extended via
 * {@code krt.web.auth-whitelist-paths} and {@code krt.web.auth-whitelist-prefixes}
 * without inventing a parallel auth stack — same {@code shouldNotFilter} path
 * permit used for login.
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
     * Paths that never require a token (login entries of the IAM user module).
     */
    private static final List<String> WHITELIST_PATHS = List.of(
            WebPaths011.IAM_USER + "/login",
            WebPaths011.IAM_USER + "/loginByUserName");

    /**
     * Path prefixes that never require a token (API docs, error page, LB probe).
     */
    private static final List<String> WHITELIST_PREFIXES = List.of(
            "/doc.html", "/webjars/", "/v3/api-docs", "/swagger-ui", "/swagger-resources", "/favicon.ico", "/error",
            "/klsjnh/open/", "/actuator/health");

    /**
     * Token port (verify only; issuing stays in the login flow).
     */
    private final AuthTokenPort authTokenPort;

    /**
     * Runtime status port (debug gate + permission PEP mode).
     */
    private final RuntimeStatusPort runtimeStatusPort;

    /**
     * Security config (extra JWT whitelist paths / prefixes).
     */
    private final KrtSecurityConfig011 securityConfig;

    /**
     * JSON mapper for the 401 / 403 envelope.
     */
    private final ObjectMapper objectMapper;

    /**
     * Create the filter.
     *
     * @param authTokenPort     token port
     * @param runtimeStatusPort runtime status port
     * @param securityConfig    security config (extra whitelist)
     * @param objectMapper      json mapper
     */
    public GlobalAuthFilter(AuthTokenPort authTokenPort, RuntimeStatusPort runtimeStatusPort,
            KrtSecurityConfig011 securityConfig, ObjectMapper objectMapper) {
        this.authTokenPort = authTokenPort;
        this.runtimeStatusPort = runtimeStatusPort;
        this.securityConfig = securityConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Skip preflight requests and the whitelisted login / docs / error / config
     * paths.
     *
     * @param request http request
     * @return true when the filter should not run
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Normalize dot segments so a raw URI like "/v3/api-docs/../klsjnh/..."
        // cannot pretend to be whitelisted while the container serves the
        // target controller. Malformed URIs fall through to the filter
        // (fail closed: the token gate runs).
        String uri = normalizeUri(request);

        if (uri == null) {
            return false;
        }

        return isJwtWhitelisted(uri);
    }

    /**
     * Whether the normalized URI is on the JWT bypass list (built-in +
     * {@code krt.web.auth-whitelist-*}).
     *
     * @param uri normalized request path
     * @return true when no token is required
     */
    boolean isJwtWhitelisted(String uri) {
        if (WHITELIST_PATHS.contains(uri) || WHITELIST_PREFIXES.stream().anyMatch(uri::startsWith)) {
            return true;
        }

        KrtSecurityConfig011.WebConfig web = securityConfig.getWeb();

        if (web == null) {
            return false;
        }

        List<String> extraPaths = web.getAuthWhitelistPaths();

        if (extraPaths != null) {
            for (String path : extraPaths) {
                if (path != null && !path.isBlank() && uri.equals(path.trim())) {
                    return true;
                }
            }
        }

        List<String> extraPrefixes = web.getAuthWhitelistPrefixes();

        if (extraPrefixes != null) {
            for (String prefix : extraPrefixes) {
                if (prefix != null && !prefix.isBlank() && uri.startsWith(prefix.trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Verify the bearer token, expose the operator id, then gate on the runtime
     * status. After the chain, enforce production permission whitelist.
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
        PermissionCheckContext011.clear();

        String traceId = resolveTraceId(request);
        MDC.put(FrameConst011.TRACE_ID, traceId);
        request.setAttribute(FrameConst011.TRACE_ID, traceId);

        OperatorIdentity identity = resolveIdentity(request);

        if (identity != null) {
            request.setAttribute(FrameConst011.OPERATOR_ID, identity.id());
            request.setAttribute(FrameConst011.OPERATOR_ACCOUNT, identity.userAccount());
        }

        try {
            if (identity == null && !runtimeStatusPort.isDebug()) {
                writeUnauthorized(request, response);
                return;
            }

            filterChain.doFilter(request, response);
            enforcePermissionWhitelist(request, response);
        } finally {
            PermissionCheckContext011.clear();
            MDC.remove(FrameConst011.TRACE_ID);
        }
    }

    /**
     * Production whitelist: mutating protected requests must have marked a
     * permission check; otherwise rewrite the response to 403 when still
     * writable.
     *
     * @param request  http request
     * @param response http response
     * @throws IOException write failure
     */
    private void enforcePermissionWhitelist(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String uri = normalizeUri(request);

        if (uri == null) {
            return;
        }

        if (!PermissionWhitelistGate011.shouldDenyUnchecked(runtimeStatusPort.isPermissionWhitelistMode(),
                request.getMethod(), uri, PermissionCheckContext011.wasChecked())) {
            return;
        }

        if (response.isCommitted()) {
            logger.warn("permission whitelist: unchecked mutating request but response committed {} {}",
                    request.getMethod(), uri);
            return;
        }

        logger.info("permission whitelist denied unchecked {} {}", request.getMethod(), uri);
        writeForbidden(request, response);
    }

    /**
     * Normalize the request URI path (dot-segment safe); null when malformed.
     *
     * @param request http request
     * @return normalized path, or null
     */
    private static String normalizeUri(HttpServletRequest request) {
        try {
            return URI.create(request.getRequestURI()).normalize().getPath();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Resolve the per-request trace id: an existing MDC value first (the
     * observability filter may have set one), then the incoming
     * {@code X-Trace-Id} header, otherwise a fresh UUID.
     *
     * @param request http request
     * @return trace id, never blank
     */
    private String resolveTraceId(HttpServletRequest request) {
        String existing = MDC.get(FrameConst011.TRACE_ID);

        if (existing != null && !existing.isBlank()) {
            return existing;
        }

        String header = request.getHeader("X-Trace-Id");

        if (header != null && !header.isBlank()) {
            return header.trim();
        }

        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Resolve the operator identity (id + account) from a bearer token, best
     * effort.
     *
     * @param request http request
     * @return operator identity, or null when the header is missing / invalid
     */
    private OperatorIdentity resolveIdentity(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        return token.isEmpty() ? null : authTokenPort.verify(token);
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
        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Response011<Void> body = Response011.unauthorized();
        body.setTraceId(MDC.get(FrameConst011.TRACE_ID));
        objectMapper.writeValue(response.getWriter(), body);
    }

    /**
     * Write the 403 envelope for an unchecked mutating request in production.
     *
     * @param request  http request
     * @param response http response
     * @throws IOException write failure
     */
    private void writeForbidden(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Response011<Void> body = Response011.of(HttpCodeEnum011.FORBIDDEN,
                "forbidden: permission not checked on this entry");
        body.setTraceId(MDC.get(FrameConst011.TRACE_ID));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
