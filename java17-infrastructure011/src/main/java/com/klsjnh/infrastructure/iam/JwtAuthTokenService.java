package com.klsjnh.infrastructure.iam;

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

import com.klsjnh.domain.iam.AuthTokenPort;
import com.klsjnh.infrastructure.config.KrtConfig011;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT adapter for the AuthTokenPort: HS256 signing with krt.jwt.secret, expiry
 * from krt.jwt.expire-minutes (KrtConfig011).
 */

@Component
public class JwtAuthTokenService implements AuthTokenPort {

    /**
     * Minimum secret length for HS256 (bytes).
     */
    private static final int MIN_SECRET_BYTES = 32;

    /**
     * Framework config (secret / expire minutes).
     */
    private final KrtConfig011 krtConfig;

    /**
     * Create the service.
     *
     * @param krtConfig framework config
     */
    public JwtAuthTokenService(KrtConfig011 krtConfig) {
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
                .claim("id", id)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }
}
