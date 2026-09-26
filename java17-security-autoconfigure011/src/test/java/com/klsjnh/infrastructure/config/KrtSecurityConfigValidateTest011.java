package com.klsjnh.infrastructure.config;

/*                KrtSecurityConfigValidateTest011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  production profile must bind krt.status=production
 *
 */

import com.klsjnh.common.enums.FrameworkStatus011;

import org.springframework.core.env.Environment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Startup guard: production profile requires {@code krt.status=production}.
 */

@ExtendWith(MockitoExtension.class)
class KrtSecurityConfigValidateTest011 {

    @Mock
    private Environment environment;

    /**
     * production profile + debug status must fail fast.
     */
    @Test
    void productionProfileRejectsNonProductionStatus() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "production" });
        KrtSecurityConfig011 config = new KrtSecurityConfig011(environment);
        config.setStatus(FrameworkStatus011.DEBUG);
        config.getJwt().setSecret("test-secret-for-guard");

        assertThrows(IllegalStateException.class, config::validate);
    }

    /**
     * production profile + development status must also fail (not only debug).
     */
    @Test
    void productionProfileRejectsDevelopmentStatus() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "production" });
        KrtSecurityConfig011 config = new KrtSecurityConfig011(environment);
        config.setStatus(FrameworkStatus011.DEVELOPMENT);
        config.getJwt().setSecret("test-secret-for-guard");

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::validate);
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("krt.status=production"));
    }

    /**
     * production profile + production status + secret boots.
     */
    @Test
    void productionProfileAcceptsProductionStatus() {
        when(environment.getActiveProfiles()).thenReturn(new String[] { "production" });
        KrtSecurityConfig011 config = new KrtSecurityConfig011(environment);
        config.setStatus(FrameworkStatus011.PRODUCTION);
        config.getJwt().setSecret("test-secret-for-guard");
        config.getCrypto().setMasterKey("dev-crypto-master-key-klsjnh-framework011-change-me-32b");

        assertDoesNotThrow(config::validate);
    }

    /**
     * production status without crypto master key must fail.
     */
    @Test
    void productionStatusRequiresCryptoMasterKey() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {});
        KrtSecurityConfig011 config = new KrtSecurityConfig011(environment);
        config.setStatus(FrameworkStatus011.PRODUCTION);
        config.getJwt().setSecret("test-secret-for-guard");

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::validate);
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("krt.crypto.master-key"));
    }
}
