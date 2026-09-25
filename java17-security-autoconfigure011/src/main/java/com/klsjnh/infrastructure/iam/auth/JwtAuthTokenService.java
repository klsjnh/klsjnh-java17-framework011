package com.klsjnh.infrastructure.iam.auth;

/*                JwtAuthTokenService class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  jwt auth token service class
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.domain.iam.auth.AuthTokenPort;
import com.klsjnh.domain.iam.auth.AuthTokenPort.OperatorIdentity;

import com.klsjnh.infrastructure.config.KrtSecurityConfig011;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT adapter for the AuthTokenPort: HS256 signing with krt.jwt.secret, expiry
 * from krt.jwt.expire-minutes (KrtSecurityConfig011).
 */

@Component
public class JwtAuthTokenService implements AuthTokenPort {

    /**
     * Minimum secret length for HS256 (bytes).
     */
    private static final int MIN_SECRET_BYTES = 32;

    /**
     * Claim name carrying the operator user id.
     */
    private static final String CLAIM_ID = "id";

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenService.class);

    /**
     * Framework config (secret / expire minutes).
     */
    private final KrtSecurityConfig011 krtConfig;

    /**
     * Create the service.
     *
     * @param krtConfig framework config
     */
    public JwtAuthTokenService(KrtSecurityConfig011 krtConfig) {
        this.krtConfig = krtConfig;
    }

    /**
     * Issue a signed token for a user.
     *
     * @param id          user id
     * @param userAccount login account
     * @return signed JWT string
     */
    @Override
    public String issue(String id, String userAccount) {
        String secret = krtConfig.getJwt().getSecret();

        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "krt.jwt.secret is required (at least " + MIN_SECRET_BYTES + " bytes for HS256)");
        }

        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + krtConfig.getJwt().getExpireMinutes() * 60_000L);

        return Jwts.builder()
                .subject(userAccount)
                .claim(CLAIM_ID, id)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Verify a signed token and return the full operator identity (id +
     * account) — what the auth filter needs to fill both audit columns.
     *
     * @param token signed JWT
     * @return operator identity, or null when missing / malformed / expired /
     *         signature-invalid
     */
    @Override
    public OperatorIdentity verify(String token) {
        Claims claims = parse(token);

        if (claims == null) {
            return null;
        }

        Object id = claims.get(CLAIM_ID);

        if (id == null) {
            return null;
        }

        return new OperatorIdentity(id.toString(), claims.getSubject());
    }

    /**
     * Verify a signed token and return its operator user id.
     *
     * @param token signed JWT
     * @return the user id claim, or null when missing / malformed / expired /
     *         signature-invalid
     */
    @Override
    public String verifyAndGetId(String token) {
        OperatorIdentity identity = verify(token);

        return identity == null ? null : identity.id();
    }

    /**
     * Parse and verify the token, returning its claims.
     *
     * @param token signed JWT
     * @return claims, null when missing / malformed / expired / invalid
     */
    private Claims parse(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String secret = krtConfig.getJwt().getSecret();

        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            return null;
        }

        try {
            SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            logger.debug("jwt verify failed {} ...", ex.getClass().getSimpleName());
            return null;
        }
    }
}
