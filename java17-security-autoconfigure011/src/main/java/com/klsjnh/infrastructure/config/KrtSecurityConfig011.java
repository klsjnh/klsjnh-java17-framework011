package com.klsjnh.infrastructure.config;

/*                KrtSecurityConfig011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  security-side krt config (split from KrtConfig011)
 *      2026.09.26  krt.web.auth-whitelist-paths / prefixes (JWT bypass)
 *      2026.09.26  krt.crypto.master-key (SecretCipher)
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import com.klsjnh.common.enums.FrameworkStatus011;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

/**
 * Security-side framework config bound to the {@code krt.*} keys: runtime
 * status, JWT settings, reversible crypto master key and web client-IP
 * settings. Lives in the security autoconfigure module; business code reads
 * config through these binding classes, never {@code @Value}. Value semantics
 * live in {@link FrameworkStatus011}, this class only binds and guards.
 */

@Slf4j
@Data
@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "krt")
public class KrtSecurityConfig011 {

    /**
     * Spring environment, used by the startup guard to inspect active profiles.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final Environment environment;

    /**
     * Runtime mode; a MISSING config resolves to PRODUCTION (fail safe: a
     * missing config must never open the auth gate). application-development.yml
     * ships with {@code debug} for development convenience — the startup guard
     * rejects that combination when the production profile is active.
     */
    private FrameworkStatus011 status = FrameworkStatus011.PRODUCTION;

    /**
     * JWT settings, default expire minutes 480.
     */
    private JwtConfig jwt = new JwtConfig();

    /**
     * Web layer settings (client IP resolution).
     */
    private WebConfig web = new WebConfig();

    /**
     * Reversible crypto settings (SecretCipher master key).
     */
    private CryptoConfig crypto = new CryptoConfig();

    /**
     * Startup guard: reject unsafe config combinations at boot.
     * <p>
     * The production Spring profile must bind {@code krt.status=production}
     * (debug / development with production profile fail fast). Production
     * status also requires a non-blank JWT secret and a crypto master key of
     * at least 32 bytes.
     * </p>
     */
    @PostConstruct
    public void validate() {
        if (hasProductionProfile() && status != FrameworkStatus011.PRODUCTION) {
            throw new IllegalStateException(
                    "production profile requires krt.status=production (got " + status.getCode() + ")");
        }

        if (status == FrameworkStatus011.PRODUCTION && jwtSecretBlank()) {
            throw new IllegalStateException("krt.jwt.secret is required in production");
        }

        if (status == FrameworkStatus011.PRODUCTION && cryptoMasterKeyInvalid()) {
            throw new IllegalStateException(
                    "krt.crypto.master-key is required in production (at least 32 bytes)");
        }

        log.info("krt.status = {} (passwordless login {}, permission PEP {})", status,
                status.allowsPasswordlessLogin() ? "enabled" : "disabled",
                status.isPermissionWhitelistMode() ? "whitelist" : "assertHas-noop");
    }

    /**
     * Whether the JWT secret is blank.
     *
     * @return true when blank
     */
    private boolean jwtSecretBlank() {
        return jwt.getSecret() == null || jwt.getSecret().isBlank();
    }

    /**
     * Whether the crypto master key is missing or shorter than 32 UTF-8 bytes.
     *
     * @return true when invalid for production
     */
    private boolean cryptoMasterKeyInvalid() {
        if (crypto == null || crypto.getMasterKey() == null || crypto.getMasterKey().isBlank()) {
            return true;
        }

        return crypto.getMasterKey().getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32;
    }

    /**
     * JWT settings.
     */
    @Data
    public static class JwtConfig {

        /**
         * HS256 signing secret, string, no default — inject via env in production.
         */
        private String secret;

        /**
         * Token expire minutes, int, default 480.
         */
        private int expireMinutes = 480;
    }

    /**
     * Reversible crypto settings for {@code SecretCipherPort}.
     */
    @Data
    public static class CryptoConfig {

        /**
         * AES master key material, string, no default — inject via env in
         * production. Must be at least 32 UTF-8 bytes when
         * {@code krt.status=production}. Hashed with SHA-256 before use.
         */
        private String masterKey;
    }

    /**
     * Web layer settings.
     */
    @Data
    public static class WebConfig {

        /**
         * Trusted proxy addresses / CIDRs in front of the app. When set, the
         * client IP resolver walks the X-Forwarded-For chain from the right and
         * returns the first untrusted hop, which defeats a forged left-most
         * entry. Empty (the default) keeps the lenient mode: the left-most
         * entry is taken as-is.
         * <p>
         * Reserved: the resolver reads it lazily, so tightening the policy
         * later needs config only, never a code change.
         * </p>
         */
        private List<String> trustedProxies = new ArrayList<>();

        /**
         * Extra exact request paths that never require a JWT. Merged with the
         * built-in login whitelist in {@code GlobalAuthFilter}. Empty by
         * default (fail closed). Typical vendor inbound callback:
         * {@code /klsjnh/messagecenter/julyInboundMessage/v1/receive}.
         * Channel-side signature verification belongs in the inbound port /
         * channel config, not in a parallel auth stack.
         */
        private List<String> authWhitelistPaths = new ArrayList<>();

        /**
         * Extra request path prefixes that never require a JWT. Merged with
         * the built-in docs / open / health prefixes in
         * {@code GlobalAuthFilter}. Empty by default (fail closed).
         */
        private List<String> authWhitelistPrefixes = new ArrayList<>();
    }

    /**
     * Whether the production profile is active.
     *
     * @return true when production profile is active
     */
    private boolean hasProductionProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("production".equalsIgnoreCase(profile)) {
                return true;
            }
        }

        return false;
    }
}
