package com.klsjnh.web.util;

/*                ClientIp011Resolver class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.10.23
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  client ip resolver (proxy headers)
 *      2026.10.23  trusted-proxy CIDR chain (anti X-Forwarded-For spoofing)
 *
 */

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Resolves the real client IP of a request.
 * <p>
 * Proxy headers are only trusted when {@code request.getRemoteAddr()} is the
 * loopback address or matches {@code krt.web.trusted-proxies}. In that case the
 * {@code X-Forwarded-For} chain is walked from the right, stripping trusted
 * hops, and the first untrusted hop is returned. A direct public caller never
 * gets to spoof its IP through a header.
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
     * Characters allowed in an IP literal. A domain name can never match this
     * set (letters are limited to hex and a colon is required for IPv6), so
     * validating before {@link InetAddress#getByName(String)} avoids a blocking
     * DNS lookup from an attacker-controlled forwarding header.
     */
    private static final Pattern IP_LITERAL = Pattern.compile("[0-9a-fA-F:.]+");

    /**
     * Configured trusted proxies (exact IPs or CIDRs).
     */
    private static volatile List<Cidr> trustedProxies = List.of();

    /**
     * Utility class, no instances.
     */
    private ClientIp011Resolver() {
    }

    /**
     * Configure trusted proxy addresses / CIDRs (IPv4 / IPv6). An empty list
     * still trusts loopback only.
     *
     * @param values exact IPs or CIDRs, nullable
     */
    public static void configureTrustedProxies(List<String> values) {
        List<Cidr> ranges = new ArrayList<>();

        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    Cidr range = Cidr.parse(value.trim());
                    if (range != null) {
                        ranges.add(range);
                    }
                }
            }
        }

        trustedProxies = List.copyOf(ranges);
    }

    /**
     * Resolve the client IP: trusted proxy chain first, then the single-value
     * headers from a trusted proxy, then the socket address.
     *
     * @param request http request, nullable (returns null when absent)
     * @return client IP, or null when the request is null
     */
    public static String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String remote = request.getRemoteAddr();

        if (!isTrusted(remote)) {
            return remote;
        }

        String forwarded = usable(request.getHeader(HEADER_FORWARDED_FOR));

        if (forwarded != null) {
            String[] chain = forwarded.split(",");
            for (int i = chain.length - 1; i >= 0; i--) {
                String hop = chain[i].trim();
                if (!isIpLiteral(hop)) {
                    continue;
                }

                if (i == 0 || !isTrusted(hop)) {
                    return hop.trim();
                }
            }
        }

        String realIp = usable(request.getHeader(HEADER_REAL_IP));

        if (isIpLiteral(realIp)) {
            return realIp.trim();
        }

        String proxyClientIp = usable(request.getHeader(HEADER_PROXY_CLIENT_IP));

        if (isIpLiteral(proxyClientIp)) {
            return proxyClientIp.trim();
        }

        String wlProxyClientIp = usable(request.getHeader(HEADER_WL_PROXY_CLIENT_IP));

        if (isIpLiteral(wlProxyClientIp)) {
            return wlProxyClientIp.trim();
        }

        return remote;
    }

    /**
     * Whether an address is loopback or matches a configured trusted proxy.
     *
     * @param ip raw address
     * @return true when trusted
     */
    private static boolean isTrusted(String ip) {
        InetAddress address = parse(ip);

        if (address == null) {
            return false;
        }

        if (address.isLoopbackAddress()) {
            return true;
        }

        byte[] bytes = normalize(address);

        for (Cidr range : trustedProxies) {
            if (range.contains(bytes)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Parse an address without DNS resolution side effects.
     *
     * @param value raw address
     * @return address, nullable
     */
    private static InetAddress parse(String value) {
        if (!isIpLiteral(value)) {
            return null;
        }

        try {
            return InetAddress.getByName(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Whether the value is an IPv4 / IPv6 literal. For the dotted form we
     * require four numeric octets (each 0-255), so an invalid numeric string
     * such as {@code 1.2.3.4.5} cannot fall through to DNS resolution.
     *
     * @param value raw address
     * @return true when the value is a numeric literal
     */
    private static boolean isIpLiteral(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String candidate = value.trim();

        if (!IP_LITERAL.matcher(candidate).matches()) {
            return false;
        }

        if (candidate.indexOf(':') >= 0) {
            return true;
        }

        String[] parts = candidate.split("\\.", -1);

        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            if (part.isEmpty() || part.length() > 3) {
                return false;
            }

            for (int i = 0; i < part.length(); i++) {
                if (!Character.isDigit(part.charAt(i))) {
                    return false;
                }
            }

            if (Integer.parseInt(part) > 255) {
                return false;
            }
        }

        return true;
    }

    /**
     * Normalize IPv4-mapped IPv6 to 4 bytes.
     *
     * @param address address
     * @return byte form
     */
    private static byte[] normalize(InetAddress address) {
        byte[] bytes = address.getAddress();

        if (bytes.length == 16) {
            boolean mapped = true;
            for (int i = 0; i < 10; i++) {
                if (bytes[i] != 0) {
                    mapped = false;
                    break;
                }
            }
            if (mapped && (bytes[10] & 0xff) == 0xff && (bytes[11] & 0xff) == 0xff) {
                return new byte[] { bytes[12], bytes[13], bytes[14], bytes[15] };
            }
        }

        return bytes;
    }

    /**
     * Return the header value unless blank / {@code unknown}.
     *
     * @param value raw header
     * @return usable value, nullable
     */
    private static String usable(String value) {
        if (value == null || value.isBlank() || UNKNOWN.equalsIgnoreCase(value.trim())) {
            return null;
        }

        return value.trim();
    }

    /**
     * One trusted CIDR range.
     */
    private static final class Cidr {

        /**
         * Network bytes.
         */
        private final byte[] network;

        /**
         * Prefix length in bits.
         */
        private final int prefix;

        /**
         * Create the range.
         *
         * @param network network bytes
         * @param prefix  prefix bits
         */
        private Cidr(byte[] network, int prefix) {
            this.network = network;
            this.prefix = prefix;
        }

        /**
         * Parse an exact IP or CIDR.
         *
         * @param value raw value
         * @return range, null when invalid
         */
        private static Cidr parse(String value) {
            String[] parts = value.split("/", 2);
            InetAddress address = ClientIp011Resolver.parse(parts[0]);

            if (address == null) {
                return null;
            }

            byte[] bytes = normalize(address);
            int max = bytes.length * 8;
            int prefix = max;

            if (parts.length == 2) {
                try {
                    prefix = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    return null;
                }
            }

            if (prefix < 0 || prefix > max) {
                return null;
            }

            return new Cidr(bytes, prefix);
        }

        /**
         * Whether the address is inside the range.
         *
         * @param address address bytes
         * @return true when inside
         */
        private boolean contains(byte[] address) {
            if (address.length != network.length) {
                return false;
            }

            int fullBytes = prefix / 8;
            int remaining = prefix % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (address[i] != network[i]) {
                    return false;
                }
            }

            if (remaining == 0) {
                return true;
            }

            int mask = 0xff << (8 - remaining);

            return (address[fullBytes] & mask) == (network[fullBytes] & mask);
        }
    }
}
