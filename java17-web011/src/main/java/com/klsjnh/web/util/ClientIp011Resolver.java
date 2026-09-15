package com.klsjnh.web.util;

/*                ClientIp011Resolver class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  client ip resolver for the audit trail
 *
 */

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the real client IP of a request.
 * <p>
 * Behind nginx / a gateway, {@code request.getRemoteAddr()} reports the proxy
 * (usually 127.0.0.1 or an intranet address), so the audit trail would record
 * the wrong actor location. The real address arrives in the standard proxy
 * headers instead:
 * </p>
 * <ul>
 *   <li>{@code X-Forwarded-For} — a comma separated chain
 *       {@code client, proxy1, proxy2}; the LEFT-most entry is the original
 *       client.</li>
 *   <li>{@code X-Real-IP} — a single-value variant written by some proxies.</li>
 *   <li>{@code Proxy-Client-IP} / {@code WL-Proxy-Client-IP} — legacy
 *       WebLogic-style headers still seen in older deployments.</li>
 * </ul>
 * <p>
 * A blank or unknown header value is treated as absent; the search falls
 * through to {@code getRemoteAddr()} last.
 * </p>
 * <p>
 * Note on trust: the left-most entry is caller-controlled and a forged value
 * is possible when the app is directly reachable. The trusted-proxy walk that
 * fixes this is reserved in {@code krt.web.trusted-proxies} (KrtConfig011) and
 * is not consumed yet — a deliberate, documented trade-off rather than an
 * oversight. Web cannot read KrtConfig011 (it sits in infrastructure), so
 * wiring that policy in later goes through a domain port.
 * </p>
 */

public final class ClientIp011Resolver {

    /**
     * Standard proxy header carrying the forwarding chain.
     */
    private static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * Single-value real-IP header.
     */
    private static final String HEADER_REAL_IP = "X-Real-IP";

    /**
     * Legacy proxy header.
     */
    private static final String HEADER_PROXY_CLIENT_IP = "Proxy-Client-IP";

    /**
     * Legacy WebLogic proxy header.
     */
    private static final String HEADER_WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";

    /**
     * Placeholder some proxies emit instead of a real address.
     */
    private static final String UNKNOWN = "unknown";

    /**
     * Utility class, no instances.
     */
    private ClientIp011Resolver() {
    }

    /**
     * Resolve the client IP: the left-most X-Forwarded-For entry first, then
     * the single-value headers, then the socket address.
     *
     * @param request http request, nullable (returns null when absent)
     * @return client IP, or null when the request is null
     */
    public static String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String forwarded = usable(request.getHeader(HEADER_FORWARDED_FOR));

        if (forwarded != null) {
            int comma = forwarded.indexOf(',');
            return (comma < 0 ? forwarded : forwarded.substring(0, comma)).trim();
        }

        String realIp = usable(request.getHeader(HEADER_REAL_IP));

        if (realIp != null) {
            return realIp;
        }

        String proxyClientIp = usable(request.getHeader(HEADER_PROXY_CLIENT_IP));

        if (proxyClientIp != null) {
            return proxyClientIp;
        }

        String wlProxyClientIp = usable(request.getHeader(HEADER_WL_PROXY_CLIENT_IP));

        if (wlProxyClientIp != null) {
            return wlProxyClientIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Trim and reject a blank / "unknown" header value.
     *
     * @param value raw header value
     * @return the trimmed value, or null when unusable
     */
    private static String usable(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() || UNKNOWN.equalsIgnoreCase(trimmed) ? null : trimmed;
    }
}
