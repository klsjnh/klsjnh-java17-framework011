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

import com.klsjnh.common.enums.FrameworkStatus011;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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
     * Runtime mode; missing config resolves to PRODUCTION (fail safe: a missing
     * config must never open the auth gate). The debug profile yaml sets it to
     * debug explicitly for development convenience.
     */
    private FrameworkStatus011 status = FrameworkStatus011.PRODUCTION;

    /**
     * Startup guard: reject unsafe config combinations at boot.
     */
    @PostConstruct
    public void validate() {
        if (status.isDebug() && hasProductionProfile()) {
            throw new IllegalStateException("krt.status=debug is not allowed with the production profile");
        }

        log.info("krt.status = {} (passwordless login {})", status,
                status.allowsPasswordlessLogin() ? "enabled" : "disabled");
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
