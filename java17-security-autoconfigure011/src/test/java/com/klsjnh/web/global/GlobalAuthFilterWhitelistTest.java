package com.klsjnh.web.global;

/*                GlobalAuthFilterWhitelistTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  unit tests for krt.web JWT whitelist merge
 *
 */

import com.klsjnh.domain.iam.auth.AuthTokenPort;
import com.klsjnh.domain.iam.auth.RuntimeStatusPort;

import com.klsjnh.infrastructure.config.KrtSecurityConfig011;

import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

/**
 * Unit tests for {@link GlobalAuthFilter#isJwtWhitelisted}: built-in paths plus
 * {@code krt.web.auth-whitelist-paths} / prefixes.
 */

class GlobalAuthFilterWhitelistTest {

    private GlobalAuthFilter filter;
    private KrtSecurityConfig011 securityConfig;

    /**
     * Builds a filter with an empty development security config.
     */
    @BeforeEach
    void setUp() {
        Environment environment = Mockito.mock(Environment.class);
        Mockito.when(environment.getActiveProfiles()).thenReturn(new String[] { "development" });
        securityConfig = new KrtSecurityConfig011(environment);
        filter = new GlobalAuthFilter(Mockito.mock(AuthTokenPort.class), Mockito.mock(RuntimeStatusPort.class),
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
}