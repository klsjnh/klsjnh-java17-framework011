package com.klsjnh.infrastructure.iam.auth;

/*                JwtAuthTokenService class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  jwt auth token service class
 *      2026.09.26  tv claim + token credential revoke check
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.iam.auth.AuthTokenPort;
import com.klsjnh.domain.iam.auth.AuthTokenPort.OperatorIdentity;
import com.klsjnh.domain.iam.auth.TokenCredentialPort;
import com.klsjnh.domain.iam.auth.TokenCredentialPort.CredentialState;

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
 * from krt.jwt.expire-minutes (KrtSecurityConfig011). Issued tokens carry claim
 * {@code tv} (token version); verify rejects version mismatch or disabled
 * accounts via {@link TokenCredentialPort}.
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
     * Claim name carrying the token version at issue time.
     */
    private static final String CLAIM_TOKEN_VERSION = "tv";

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenService.class);

    /**
     * Framework config (secret / expire minutes).
     */
    private final KrtSecurityConfig011 krtConfig;

    /**
     * Durable credential lookup (token version + status).
     */
    private final TokenCredentialPort tokenCredentialPort;

    /**
     * Create the service.
     *
     * @param krtConfig            framework config
     * @param tokenCredentialPort  credential lookup for revoke checks
     */
    public JwtAuthTokenService(KrtSecurityConfig011 krtConfig, TokenCredentialPort tokenCredentialPort) {
        this.krtConfig = krtConfig;
        this.tokenCredentialPort = tokenCredentialPort;
    }

    /**
     * Issue a signed token for a user.
     *
     * @param id           user id
     * @param userAccount  login account
     * @param tokenVersion current token version at issue time
     * @return signed JWT string
     */
    @Override
    public String issue(String id, String userAccount, int tokenVersion) {
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
                .claim(CLAIM_TOKEN_VERSION, tokenVersion)
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
     *         signature-invalid / version-mismatched / account-disabled
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

        String userId = id.toString();
        int tokenVersion = readTokenVersion(claims);
        CredentialState state = tokenCredentialPort.findByUserId(userId);

        if (state == null) {
            logger.debug("jwt verify rejected: user missing {}", userId);
            return null;
        }

        if (Status011.DISABLED.getCode().equals(state.status())) {
            logger.debug("jwt verify rejected: user disabled {}", userId);
            return null;
        }

        if (state.tokenVersion() != tokenVersion) {
            logger.debug("jwt verify rejected: token version mismatch user={} claim={} db={}", userId, tokenVersion,
                    state.tokenVersion());
            return null;
        }

        return new OperatorIdentity(userId, claims.getSubject());
    }

    /**
     * Verify a signed token and return its operator user id.
     *
     * @param token signed JWT
     * @return the user id claim, or null when missing / malformed / expired /
     *         signature-invalid / version-mismatched / account-disabled
     */
    @Override
    public String verifyAndGetId(String token) {
        OperatorIdentity identity = verify(token);

        return identity == null ? null : identity.id();
    }

    /**
     * Read the {@code tv} claim; missing claim is treated as 0 (legacy tokens).
     *
     * @param claims parsed claims
     * @return token version
     */
    private static int readTokenVersion(Claims claims) {
        Object raw = claims.get(CLAIM_TOKEN_VERSION);

        if (raw == null) {
            return 0;
        }

        if (raw instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(raw.toString());
        } catch (NumberFormatException ex) {
            return -1;
        }
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
