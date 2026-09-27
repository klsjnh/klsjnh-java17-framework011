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
 *      2026.09.26  fail-closed insecure status + reject default secrets
 *      2026.09.26  bind krt.outbound to OutboundUrlGuard011
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import com.klsjnh.common.enums.FrameworkStatus011;
import com.klsjnh.common.util.OutboundUrlGuard011;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Security-side framework config bound to the {@code krt.*} keys: runtime
 * status, JWT settings, reversible crypto master key, outbound URL policy and
 * web client-IP settings. Lives in the security autoconfigure module; business
 * code reads config through these binding classes, never {@code @Value}. Value
 * semantics live in {@link FrameworkStatus011}, this class only binds and
 * guards.
 */

@Slf4j
@Data
@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "krt")
public class KrtSecurityConfig011 {

    /**
     * Known repository / .env.example development defaults that must never
     * boot under {@code krt.status=production}.
     */
    private static final Set<String> KNOWN_DEV_SECRETS = Set.of(
            "dev-secret-klsjnh-framework011-20260912-change-me-in-production-0123456789",
            "dev-secret-klsjnh-framework011-change-me-32bytes",
            "dev-crypto-master-key-klsjnh-framework011-change-me-32b");

    /**
     * Spring environment, used by the startup guard to inspect active profiles.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final Environment environment;

    /**
     * Runtime mode; a MISSING config resolves to PRODUCTION (fail safe: a
     * missing config must never open the auth gate). application-development.yml
     * ships with {@code debug} plus {@code krt.allow-insecure-status=true} for
     * local convenience — deploy without that opt-in fails closed.
     */
    private FrameworkStatus011 status = FrameworkStatus011.PRODUCTION;

    /**
     * Explicit opt-in required when {@code krt.status} is not production.
     * Development yml sets this true; bare deploy / missing yml cannot open
     * the auth gate by accident.
     */
    private boolean allowInsecureStatus = false;

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
     * Outbound HTTP URL policy (SSRF guard shared by HttpUtil011 callers).
     */
    private OutboundConfig outbound = new OutboundConfig();

    /**
     * Startup guard: reject unsafe config combinations at boot.
     * <p>
     * Fail-closed rules:
     * </p>
     * <ul>
     * <li>Spring {@code production} profile requires {@code krt.status=production}</li>
     * <li>Non-production status requires {@code krt.allow-insecure-status=true}</li>
     * <li>Production status rejects blank / known-dev / {@code change-me} secrets</li>
     * <li>Production status rejects {@code krt.outbound.allow-private=true}</li>
     * </ul>
     */
    @PostConstruct
    public void validate() {
        if (hasProductionProfile() && status != FrameworkStatus011.PRODUCTION) {
            throw new IllegalStateException(
                    "production profile requires krt.status=production (got " + status.getCode() + ")");
        }

        if (status != FrameworkStatus011.PRODUCTION && !allowInsecureStatus) {
            throw new IllegalStateException(
                    "krt.status=" + status.getCode()
                            + " requires krt.allow-insecure-status=true (fail-closed: refuse open auth)");
        }

        if (status == FrameworkStatus011.PRODUCTION && jwtSecretBlank()) {
            throw new IllegalStateException("krt.jwt.secret is required in production");
        }

        if (status == FrameworkStatus011.PRODUCTION && cryptoMasterKeyInvalid()) {
            throw new IllegalStateException(
                    "krt.crypto.master-key is required in production (at least 32 bytes)");
        }

        if (status == FrameworkStatus011.PRODUCTION && isInsecureSecret(jwt.getSecret())) {
            throw new IllegalStateException(
                    "krt.jwt.secret looks like a repository default; set a unique production secret");
        }

        if (status == FrameworkStatus011.PRODUCTION && isInsecureSecret(crypto.getMasterKey())) {
            throw new IllegalStateException(
                    "krt.crypto.master-key looks like a repository default; set a unique production key");
        }

        if (status == FrameworkStatus011.PRODUCTION && outbound != null && outbound.isAllowPrivate()) {
            throw new IllegalStateException("krt.outbound.allow-private must be false in production");
        }

        applyOutboundPolicy();

        if (status.isDebug()) {
            log.warn("**************************************************************");
            log.warn("* krt.status=debug: auth filter does NOT reject requests;    *");
            log.warn("* AuthorizationPort.assertHas is a no-op. NEVER ship this.  *");
            log.warn("**************************************************************");
        }

        log.info("krt.status = {} (passwordless login {}, permission PEP {})", status,
                status.allowsPasswordlessLogin() ? "enabled" : "disabled",
                status.isPermissionWhitelistMode() ? "whitelist" : "assertHas-noop");
    }

    /**
     * Push outbound settings into the shared {@link OutboundUrlGuard011}.
     */
    private void applyOutboundPolicy() {
        OutboundConfig cfg = outbound == null ? new OutboundConfig() : outbound;
        boolean allowPrivate = cfg.isAllowPrivate() && status != FrameworkStatus011.PRODUCTION;
        OutboundUrlGuard011.configure(new OutboundUrlGuard011.Policy(cfg.isAllowHttp(), allowPrivate,
                cfg.getMaxResponseBytes()));
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
     * Whether a secret matches a known repository default or contains
     * {@code change-me}.
     *
     * @param value raw secret
     * @return true when unsafe for production
     */
    static boolean isInsecureSecret(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String trimmed = value.trim();
        if (KNOWN_DEV_SECRETS.contains(trimmed)) {
            return true;
        }

        return trimmed.toLowerCase(Locale.ROOT).contains("change-me");
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
     * Outbound HTTP URL policy bound to {@code krt.outbound}.
     */
    @Data
    public static class OutboundConfig {

        /**
         * Allow plain {@code http} (default false; https only).
         */
        private boolean allowHttp = false;

        /**
         * Allow loopback / private / link-local targets. Production startup
         * rejects {@code true}; development yml may enable for local tests.
         */
        private boolean allowPrivate = false;

        /**
         * Max bytes for {@code HttpUtil011} byte downloads / binary responses.
         */
        private int maxResponseBytes = 10 * 1024 * 1024;
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
