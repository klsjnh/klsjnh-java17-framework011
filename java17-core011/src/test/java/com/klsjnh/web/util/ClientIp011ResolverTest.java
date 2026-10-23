package com.klsjnh.web.util;

/*                ClientIp011ResolverTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.23
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.23  trusted proxy / X-Forwarded-For spoofing tests
 *
 */

import org.springframework.mock.web.MockHttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

/**
 * Unit tests for trusted-proxy-aware client IP resolution.
 */

class ClientIp011ResolverTest {

    /**
     * Reset the static trusted proxy list between cases.
     */
    @AfterEach
    void tearDown() {
        ClientIp011Resolver.configureTrustedProxies(List.of());
    }

    /**
     * A direct public caller cannot spoof its address via a header.
     */
    @Test
    void untrustedRemoteHeaderIsIgnored() {
        MockHttpServletRequest request = request("198.51.100.7");
        request.addHeader("X-Forwarded-For", "203.0.113.9");
        request.addHeader("X-Real-IP", "203.0.113.10");

        assertEquals("198.51.100.7", ClientIp011Resolver.resolve(request));
    }

    /**
     * A domain name in a forwarding header is ignored instead of triggering a
     * blocking DNS lookup.
     */
    @Test
    void domainForwardedHeaderIsIgnored() {
        ClientIp011Resolver.configureTrustedProxies(List.of("10.0.0.0/8"));

        MockHttpServletRequest request = request("10.1.2.3");
        request.addHeader("X-Forwarded-For", "evil.example.com");
        request.addHeader("X-Real-IP", "evil.example.com");

        assertEquals("10.1.2.3", ClientIp011Resolver.resolve(request));
    }

    /**
     * A trusted proxy's X-Forwarded-For chain is walked from the right; the
     * first untrusted hop is the client.
     */
    @Test
    void trustedProxyChainReturnsFirstUntrustedHop() {
        ClientIp011Resolver.configureTrustedProxies(List.of("10.0.0.5"));

        MockHttpServletRequest request = request("10.0.0.5");
        request.addHeader("X-Forwarded-For", "203.0.113.9, 10.0.0.5");

        assertEquals("203.0.113.9", ClientIp011Resolver.resolve(request));
    }

    /**
     * CIDR trusted proxy ranges are supported.
     */
    @Test
    void trustedProxyCidrIsSupported() {
        ClientIp011Resolver.configureTrustedProxies(List.of("10.0.0.0/8"));

        MockHttpServletRequest request = request("10.2.3.4");
        request.addHeader("X-Forwarded-For", "203.0.113.9");

        assertEquals("203.0.113.9", ClientIp011Resolver.resolve(request));
    }

    /**
     * Loopback is always trusted (local reverse proxy).
     */
    @Test
    void loopbackIsTrusted() {
        MockHttpServletRequest request = request("127.0.0.1");
        request.addHeader("X-Forwarded-For", "203.0.113.9");

        assertEquals("203.0.113.9", ClientIp011Resolver.resolve(request));
    }

    /**
     * A trusted proxy without forwarding headers yields the socket address.
     */
    @Test
    void trustedProxyWithoutHeadersFallsBackToRemote() {
        ClientIp011Resolver.configureTrustedProxies(List.of("10.0.0.0/8"));

        assertEquals("10.1.2.3", ClientIp011Resolver.resolve(request("10.1.2.3")));
    }

    /**
     * X-Real-IP is used only when the peer is a trusted proxy.
     */
    @Test
    void trustedProxyUsesRealIpHeader() {
        ClientIp011Resolver.configureTrustedProxies(List.of("10.0.0.0/8"));

        MockHttpServletRequest request = request("10.1.2.3");
        request.addHeader("X-Real-IP", "203.0.113.9");

        assertEquals("203.0.113.9", ClientIp011Resolver.resolve(request));
    }

    /**
     * IPv6 loopback is trusted.
     */
    @Test
    void ipv6LoopbackIsTrusted() {
        MockHttpServletRequest request = request("0:0:0:0:0:0:0:1");
        request.addHeader("X-Forwarded-For", "2001:db8::7");

        assertEquals("2001:db8::7", ClientIp011Resolver.resolve(request));
    }

    /**
     * Build a request with a socket peer address.
     *
     * @param remoteAddr socket peer
     * @return mock request
     */
    private static MockHttpServletRequest request(String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/klsjnh/iam/julyUser/v1/getById");
        request.setRemoteAddr(remoteAddr);

        return request;
    }
}
