package com.klsjnh.infrastructure.config;

/*                KrtSecurityConfigValidateTest011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  production profile must bind krt.status=production
 *      2026.09.26  fail-closed insecure status + reject default secrets
 *
 */

import com.klsjnh.common.enums.FrameworkStatus011;
import com.klsjnh.common.util.OutboundUrlGuard011;

import org.springframework.core.env.Environment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Startup guard: production profile / status, insecure-status opt-in, and
 * rejection of repository default secrets.
 */

@ExtendWith(MockitoExtension.class)
class KrtSecurityConfigValidateTest011 {

    /**
     * Production-grade JWT secret used by happy-path tests (no change-me).
     */
    private static final String PROD_JWT = "prod-test-jwt-secret-klsjnh-framework011-unique-value-0123456789ab";

    /**
     * Production-grade crypto master key used by happy-path tests.
     */
    private static final String PROD_CRYPTO = "prod-test-crypto-master-key-klsjnh-framework011-ok-32b!";

    @Mock
    private Environment environment;

    @AfterEach
    void resetOutboundPolicy() {
        OutboundUrlGuard011.configure(OutboundUrlGuard011.Policy.defaults());
    }

    /**
     * production profile + debug status must fail fast.
     */
    @Test
    void productionProfileRejectsNonProductionStatus() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "production" });
        KrtSecurityConfig011 config = baseConfig(FrameworkStatus011.DEBUG);
        config.setAllowInsecureStatus(true);

        assertThrows(IllegalStateException.class, config::validate);
    }

    /**
     * production profile + development status must also fail (not only debug).
     */
    @Test
    void productionProfileRejectsDevelopmentStatus() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "production" });
        KrtSecurityConfig011 config = baseConfig(FrameworkStatus011.DEVELOPMENT);
        config.setAllowInsecureStatus(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::validate);
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("krt.status=production"));
    }

    /**
     * production profile + production status + unique secrets boots.
     */
    @Test
    void productionProfileAcceptsProductionStatus() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "production" });
        KrtSecurityConfig011 config = baseConfig(FrameworkStatus011.PRODUCTION);

        assertDoesNotThrow(config::validate);
        assertFalse(OutboundUrlGuard011.policy().allowPrivate());
    }

    /**
     * production status without crypto master key must fail.
     */
    @Test
    void productionStatusRequiresCryptoMasterKey() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {});
        KrtSecurityConfig011 config = new KrtSecurityConfig011(environment);
        config.setStatus(FrameworkStatus011.PRODUCTION);
        config.getJwt().setSecret(PROD_JWT);

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::validate);
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("krt.crypto.master-key"));
    }

    /**
     * production + repository default JWT secret must fail.
     */
    @Test
    void productionRejectsKnownDevJwtSecret() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {});
        KrtSecurityConfig011 config = baseConfig(FrameworkStatus011.PRODUCTION);
        config.getJwt().setSecret(
                "dev-secret-klsjnh-framework011-20260912-change-me-in-production-0123456789");

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::validate);
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("krt.jwt.secret"));
    }

    /**
     * production + any secret containing change-me must fail.
     */
    @Test
    void productionRejectsChangeMeSecret() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {});
        KrtSecurityConfig011 config = baseConfig(FrameworkStatus011.PRODUCTION);
        config.getJwt().setSecret("please-change-me-before-shipping-this-value-now");

        assertThrows(IllegalStateException.class, config::validate);
    }

    /**
     * debug without allow-insecure-status fails closed.
     */
    @Test
    void debugWithoutAllowInsecureFails() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "development" });
        KrtSecurityConfig011 config = new KrtSecurityConfig011(environment);
        config.setStatus(FrameworkStatus011.DEBUG);
        config.setAllowInsecureStatus(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::validate);
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("allow-insecure-status"));
    }

    /**
     * debug with explicit allow-insecure-status boots (local/dev yml path).
     */
    @Test
    void debugWithAllowInsecureBoots() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "development" });
        KrtSecurityConfig011 config = new KrtSecurityConfig011(environment);
        config.setStatus(FrameworkStatus011.DEBUG);
        config.setAllowInsecureStatus(true);
        config.getOutbound().setAllowPrivate(true);
        config.getOutbound().setAllowHttp(true);

        assertDoesNotThrow(config::validate);
        assertTrue(OutboundUrlGuard011.policy().allowPrivate());
        assertTrue(OutboundUrlGuard011.policy().allowHttp());
    }

    /**
     * production must reject allow-private outbound.
     */
    @Test
    void productionRejectsAllowPrivateOutbound() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {});
        KrtSecurityConfig011 config = baseConfig(FrameworkStatus011.PRODUCTION);
        config.getOutbound().setAllowPrivate(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::validate);
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("allow-private"));
    }

    /**
     * Build a production-ready config with unique secrets.
     *
     * @param status runtime status
     * @return config
     */
    private KrtSecurityConfig011 baseConfig(FrameworkStatus011 status) {
        KrtSecurityConfig011 config = new KrtSecurityConfig011(environment);
        config.setStatus(status);
        config.getJwt().setSecret(PROD_JWT);
        config.getCrypto().setMasterKey(PROD_CRYPTO);
        return config;
    }
}
