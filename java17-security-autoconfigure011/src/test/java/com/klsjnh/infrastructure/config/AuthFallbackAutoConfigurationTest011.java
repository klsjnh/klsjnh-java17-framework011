package com.klsjnh.infrastructure.config;

/*                AuthFallbackAutoConfigurationTest011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  assembly test: authorization fallback must not be fail-open
 *
 */

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.auth.RuntimeStatusPort;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Assembly tests for the authorization fallback auto configuration: the
 * fallback port must be an explicit, debug-only, ordered opt-in — never a
 * silent fail-open default (audit §2.1).
 */

class AuthFallbackAutoConfigurationTest011 {

    /**
     * Runtime status stub reporting the given debug flag.
     *
     * @param debug whether the stub reports debug
     * @return runtime status stub
     */
    private static RuntimeStatusPort status(boolean debug, boolean production) {
        RuntimeStatusPort port = Mockito.mock(RuntimeStatusPort.class);
        Mockito.when(port.isDebug()).thenReturn(debug);
        Mockito.when(port.isProduction()).thenReturn(production);

        return port;
    }

    /**
     * Outside production the fallback is assembled even without the explicit
     * property (matchIfMissing = true).
     */
    @Test
    void assembledWithoutOptInOutsideProduction() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AuthFallbackAutoConfiguration011.class))
                .withBean(RuntimeStatusPort.class, () -> status(true, false))
                .run(context -> Assertions.assertNotNull(context.getBean(AuthorizationPort.class)));
    }

    /**
     * With the opt-in in debug the fallback is assembled and allows checks.
     */
    @Test
    void assembledWhenOptedInInDebug() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AuthFallbackAutoConfiguration011.class))
                .withBean(RuntimeStatusPort.class, () -> status(true, false))
                .withPropertyValues("krt.security.permissive=true")
                .run(context -> {
                    Assertions.assertNull(context.getStartupFailure());
                    Assertions.assertTrue(context.getBean(AuthorizationPort.class).has("u1", "any"));
                });
    }

    /**
     * Production still refuses the permissive fallback (fail-closed).
     */
    @Test
    void refusesToStartInProduction() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AuthFallbackAutoConfiguration011.class))
                .withBean(RuntimeStatusPort.class, () -> status(false, true))
                .withPropertyValues("krt.security.permissive=true")
                .run(context -> Assertions.assertNotNull(context.getStartupFailure()));
    }
}
