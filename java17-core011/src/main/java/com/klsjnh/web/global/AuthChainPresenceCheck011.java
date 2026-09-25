package com.klsjnh.web.global;

/*                AuthChainPresenceCheck011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  fail-fast guard: web app without the security chain must
 *                  not start (management endpoints would be unauthenticated)
 *
 */

import com.klsjnh.domain.iam.auth.AuthTokenPort;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

/**
 * Startup guard: a servlet web application that carries the framework core
 * MUST also carry the security chain. A missing {@link AuthTokenPort} means
 * the security starter was not included and every management endpoint would
 * start unauthenticated, so the boot fails fast instead of silently opening
 * the API. Default posture is fail closed.
 */

@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthChainPresenceCheck011 implements SmartInitializingSingleton {

    /**
     * Token port provider, empty when the security starter is absent.
     */
    private final ObjectProvider<AuthTokenPort> authTokenPort;

    /**
     * Create the guard.
     *
     * @param authTokenPort token port provider
     */
    public AuthChainPresenceCheck011(ObjectProvider<AuthTokenPort> authTokenPort) {
        this.authTokenPort = authTokenPort;
    }

    /** {@inheritDoc} */
    @Override
    public void afterSingletonsInstantiated() {
        if (authTokenPort.getIfAvailable() == null) {
            throw new IllegalStateException(
                    "security starter missing: no AuthTokenPort bean on the classpath — add "
                            + "java17-security-starter011; web endpoints must never start unauthenticated");
        }
    }
}
