package com.klsjnh.common.util;

/*                OutboundUrlGuard011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  shared outbound url ssrf guard
 *
 */

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Shared outbound URL guard for framework HTTP clients (webhook, ASR media
 * download, AI vendor calls). Rejects non-http(s) schemes and, after DNS
 * resolution, loopback / link-local / site-local / any-local / multicast /
 * IPv6 unique-local targets unless {@link Policy#allowPrivate()} is enabled
 * (development only).
 */

public final class OutboundUrlGuard011 {

    /**
     * Active policy (defaults fail closed: https only, no private hosts).
     */
    private static volatile Policy policy = Policy.defaults();

    /**
     * Utility holder, no instances.
     */
    private OutboundUrlGuard011() {
    }

    /**
     * Replace the active outbound policy (called from security startup guard).
     *
     * @param next next policy, null resets to defaults
     */
    public static void configure(Policy next) {
        policy = next == null ? Policy.defaults() : next;
    }

    /**
     * Current outbound policy.
     *
     * @return active policy, never null
     */
    public static Policy policy() {
        return policy;
    }

    /**
     * Assert the URL is safe to fetch under the active policy.
     *
     * @param url raw request URL
     * @throws IllegalArgumentException when the URL is blank, uses a forbidden
     *                                  scheme, or resolves to a blocked address
     */
    public static void assertSafe(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("outbound url is required");
        }

        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("outbound url is malformed", ex);
        }

        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        Policy active = policy;

        if ("https".equals(scheme)) {
            // allowed
        } else if ("http".equals(scheme)) {
            if (!active.allowHttp()) {
                throw new IllegalArgumentException("outbound url must use https");
            }
        } else {
            throw new IllegalArgumentException("outbound url scheme is not allowed: " + scheme);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("outbound url host is required");
        }

        if (active.allowPrivate()) {
            return;
        }

        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException ex) {
            throw new IllegalArgumentException("outbound url host cannot be resolved", ex);
        }

        if (addresses.length == 0) {
            throw new IllegalArgumentException("outbound url host cannot be resolved");
        }

        for (InetAddress address : addresses) {
            if (isBlockedAddress(address)) {
                throw new IllegalArgumentException("outbound url target is not allowed");
            }
        }
    }

    /**
     * Whether the address is loopback / private / link-local / multicast /
     * any-local / IPv6 unique-local.
     *
     * @param address resolved address
     * @return true when blocked under the default policy
     */
    static boolean isBlockedAddress(InetAddress address) {
        if (address == null) {
            return true;
        }

        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }

        if (address instanceof Inet6Address inet6) {
            byte[] bytes = inet6.getAddress();
            // fc00::/7 unique local
            if (bytes.length >= 1 && (bytes[0] & 0xfe) == 0xfc) {
                return true;
            }
        }

        if (address instanceof Inet4Address inet4) {
            byte[] bytes = inet4.getAddress();
            // 0.0.0.0/8 already covered by isAnyLocal; keep CGNAT 100.64.0.0/10
            if (bytes.length == 4 && (bytes[0] & 0xff) == 100 && (bytes[1] & 0xc0) == 64) {
                return true;
            }
        }

        return false;
    }

    /**
     * Outbound URL policy.
     *
     * @param allowHttp         whether plain http is allowed (default false)
     * @param allowPrivate      whether private / loopback targets are allowed
     * @param maxResponseBytes  hard cap for byte-oriented downloads
     */
    public record Policy(boolean allowHttp, boolean allowPrivate, int maxResponseBytes) {

        /**
         * Fail-closed defaults: https only, no private hosts, 10 MiB body cap.
         *
         * @return default policy
         */
        public static Policy defaults() {
            return new Policy(false, false, 10 * 1024 * 1024);
        }
    }
}
