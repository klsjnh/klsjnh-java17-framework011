package com.klsjnh.infrastructure.iam.auth;

/*                JwtAuthTokenServiceTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  jwt issue/verify with token version bump
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.iam.auth.AuthTokenPort.OperatorIdentity;
import com.klsjnh.domain.iam.auth.TokenCredentialPort;
import com.klsjnh.domain.iam.auth.TokenCredentialPort.CredentialState;

import com.klsjnh.infrastructure.config.KrtSecurityConfig011;

import org.springframework.core.env.Environment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JwtAuthTokenService}: issue embeds {@code tv}; verify
 * rejects version mismatch and disabled accounts.
 */

@ExtendWith(MockitoExtension.class)
class JwtAuthTokenServiceTest {

    private static final String JWT_SECRET = "dev-secret-klsjnh-framework011-change-me-32bytes";

    @Mock
    private Environment environment;

    @Mock
    private TokenCredentialPort tokenCredentialPort;

    private JwtAuthTokenService service;

    /**
     * Wire the service with a valid JWT secret.
     */
    @BeforeEach
    void setUp() {
        KrtSecurityConfig011 config = new KrtSecurityConfig011(environment);
        config.getJwt().setSecret(JWT_SECRET);
        config.getJwt().setExpireMinutes(60);
        service = new JwtAuthTokenService(config, tokenCredentialPort);
    }

    /**
     * Issue then verify succeeds when version and status match.
     */
    @Test
    void issueAndVerifySucceedsWithMatchingVersion() {
        when(tokenCredentialPort.findByUserId("u1"))
                .thenReturn(new CredentialState(3, Status011.ENABLED.getCode()));

        String token = service.issue("u1", "demo", 3);
        OperatorIdentity identity = service.verify(token);

        assertNotNull(identity);
        assertEquals("u1", identity.id());
        assertEquals("demo", identity.userAccount());
    }

    /**
     * After a version bump, the old token fails verify.
     */
    @Test
    void verifyRejectsAfterVersionBump() {
        String token = service.issue("u1", "demo", 1);
        when(tokenCredentialPort.findByUserId("u1"))
                .thenReturn(new CredentialState(2, Status011.ENABLED.getCode()));

        assertNull(service.verify(token));
    }

    /**
     * Disabled account fails verify even when the version still matches.
     */
    @Test
    void verifyRejectsDisabledAccount() {
        String token = service.issue("u1", "demo", 0);
        when(tokenCredentialPort.findByUserId("u1"))
                .thenReturn(new CredentialState(0, Status011.DISABLED.getCode()));

        assertNull(service.verify(token));
    }

    /**
     * Missing user fails verify.
     */
    @Test
    void verifyRejectsMissingUser() {
        String token = service.issue("gone", "demo", 0);
        when(tokenCredentialPort.findByUserId("gone")).thenReturn(null);

        assertNull(service.verify(token));
    }
}
