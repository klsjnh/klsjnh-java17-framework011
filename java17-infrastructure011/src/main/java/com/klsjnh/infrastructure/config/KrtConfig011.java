package com.klsjnh.infrastructure.config;

/*                KrtConfig011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  krt config 011 class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import com.klsjnh.common.enums.FrameworkStatus011;

import com.klsjnh.domain.datasource.ConnectionInfo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

/**
 * Framework config bound to the {@code krt.*} keys in application yaml.
 * <p>
 * Single configuration entry for framework runtime keys; business code MUST
 * read config through this class, never {@code @Value}. Only {@code krt.status}
 * is bound for now; more keys join here as the framework grows. The value
 * semantics live in {@link FrameworkStatus011} (common), this class only binds
 * and guards.
 * </p>
 */

@Slf4j
@Data
@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "krt")
public class KrtConfig011 {

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
     * Dynamic datasource connection list (krt.ci011) — declared at startup,
     * pools connect lazily on first routing call.
     */
    private List<ConnectionInfo> ci011 = new ArrayList<>();


    /**
     * AI settings (krt.ai011.*).
     */
    private AiConfig ai011 = new AiConfig();

    /**
     * Startup guard: reject unsafe config combinations at boot.
     */
    @PostConstruct
    public void validate() {
        if (status.isDebug() && hasProductionProfile()) {
            throw new IllegalStateException("krt.status=debug is not allowed with the production profile");
        }

        if (status == FrameworkStatus011.PRODUCTION && jwtSecretBlank()) {
            throw new IllegalStateException("krt.jwt.secret is required in production");
        }

        log.info("krt.status = {} (passwordless login {})", status,
                status.allowsPasswordlessLogin() ? "enabled" : "disabled");
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
    }

    /**
     * AI settings.
     */
    @Data
    public static class AiConfig {

        /**
         * Chat request timeout seconds (krt.ai011.chat-timeout-seconds).
         */
        private long chatTimeoutSeconds = 60;
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
