package com.klsjnh.web.global;

/*                GlobalAuthFilterWhitelistTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.10.05
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  unit tests for krt.web JWT whitelist merge
 *      2026.10.05  api docs blocked in production (404) unit tests
 *
 */

import com.klsjnh.domain.iam.auth.AuthTokenPort;
import com.klsjnh.domain.iam.auth.RuntimeStatusPort;

import com.klsjnh.infrastructure.config.KrtSecurityConfig011;

import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

/**
 * Unit tests for {@link GlobalAuthFilter#isJwtWhitelisted}: built-in paths plus
 * {@code krt.web.auth-whitelist-paths} / prefixes, and the production API docs
 * block (404 whatever the token).
 */

class GlobalAuthFilterWhitelistTest {

    private GlobalAuthFilter filter;
    private KrtSecurityConfig011 securityConfig;
    private RuntimeStatusPort runtimeStatusPort;

    /**
     * Builds a filter with an empty development security config.
     */
    @BeforeEach
    void setUp() {
        Environment environment = Mockito.mock(Environment.class);
        Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] { "development" });
        securityConfig = new KrtSecurityConfig011(environment);
        runtimeStatusPort = Mockito.mock(RuntimeStatusPort.class);
        filter = new GlobalAuthFilter(Mockito.mock(AuthTokenPort.class), runtimeStatusPort,
                securityConfig, new ObjectMapper());
    }

    /**
     * Built-in login path remains whitelisted without extra config.
     */
    @Test
    void builtInLoginPathIsWhitelisted() {
        assertTrue(filter.isJwtWhitelisted(WebPaths011.IAM_USER + "/login"));
    }

    /**
     * Inbound receive is not whitelisted unless configured.
     */
    @Test
    void receiveNotWhitelistedByDefault() {
        assertFalse(filter.isJwtWhitelisted(WebPaths011.MESSAGE_INBOUND_RECEIVE));
    }

    /**
     * Exact path from {@code auth-whitelist-paths} is whitelisted.
     */
    @Test
    void configuredExactPathIsWhitelisted() {
        securityConfig.getWeb().setAuthWhitelistPaths(List.of(WebPaths011.MESSAGE_INBOUND_RECEIVE));

        assertTrue(filter.isJwtWhitelisted(WebPaths011.MESSAGE_INBOUND_RECEIVE));
        assertFalse(filter.isJwtWhitelisted(WebPaths011.MESSAGE_INBOUND_MESSAGE + "/selectListByPage"));
    }

    /**
     * Prefix from {@code auth-whitelist-prefixes} covers nested paths.
     */
    @Test
    void configuredPrefixIsWhitelisted() {
        securityConfig.getWeb().setAuthWhitelistPrefixes(List.of("/klsjnh/messagecenter/julyInboundMessage/"));

        assertTrue(filter.isJwtWhitelisted(WebPaths011.MESSAGE_INBOUND_RECEIVE));
    }

    /**
     * The docs whitelist fact itself is unchanged (the block is a
     * production-mode layer on top of it).
     */
    @Test
    void apiDocsRemainOnTheWhitelistFact() {
        assertTrue(filter.isJwtWhitelisted("/doc.html"));
        assertTrue(filter.isJwtWhitelisted("/v3/api-docs/x"));
    }

    /**
     * In production an api docs request is answered with a 404 envelope and
     * never reaches the chain — even with the whitelist fact true.
     *
     * @throws Exception servlet failure
     */
    @Test
    void productionBlocksApiDocsWith404() throws Exception {
        when(runtimeStatusPort.isProduction()).thenReturn(true);
        when(runtimeStatusPort.isDebug()).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/doc.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(404, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    /**
     * In development api docs stay anonymous and reachable (no regression).
     *
     * @throws Exception servlet failure
     */
    @Test
    void developmentServesApiDocsAnonymously() throws Exception {
        when(runtimeStatusPort.isProduction()).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/doc.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(chain).doFilter(request, response);
    }
}