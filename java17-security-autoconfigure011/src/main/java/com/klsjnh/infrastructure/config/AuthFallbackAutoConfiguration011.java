package com.klsjnh.infrastructure.config;

/*                AuthFallbackAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  authorization fallback auto configuration (ordered, no bare component)
 *
 */

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.auth.RuntimeStatusPort;

import com.klsjnh.infrastructure.iam.auth.PermissiveAuthorizationPort;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Authorization fallback auto configuration — the fallback port is registered
 * here as an ordered {@code @Bean}, never as a bare scanned {@code @Component}.
 * <p>
 * Why ordered: {@code @ConditionalOnMissingBean} is evaluated against the bean
 * definitions registered so far, so a scanned component's outcome depends on
 * scan order (Spring documents this). Running <i>after</i> the access center's
 * auto configuration makes the decision deterministic: if the center registered
 * a real {@link AuthorizationPort}, the fallback backs off; otherwise it is the
 * only candidate. Two ports can never coexist.
 * </p>
 * <p>
 * The bean is additionally gated by the explicit {@code
 * krt.security.permissive=true} opt-in, and {@link PermissiveAuthorizationPort}
 * refuses to start outside debug — so a user-only assembly fails closed instead
 * of silently allowing every permission check.
 * </p>
 */

@AutoConfiguration(afterName = "com.klsjnh.enabled.AccessCenterAutoConfiguration011")
@ConditionalOnProperty(name = "krt.security.permissive", havingValue = "true")
public class AuthFallbackAutoConfiguration011 {

    /**
     * Register the permissive fallback when no other authorization port exists.
     *
     * @param runtimeStatusPort runtime status port
     * @return fallback authorization port
     */
    @Bean
    @ConditionalOnMissingBean(AuthorizationPort.class)
    public AuthorizationPort permissiveAuthorizationPort(RuntimeStatusPort runtimeStatusPort) {
        return new PermissiveAuthorizationPort(runtimeStatusPort);
    }
}
