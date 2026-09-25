package com.klsjnh.infrastructure.iam.auth;

/*                RuntimeStatusAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  runtime status adapter class
 *
 */

import com.klsjnh.domain.iam.auth.RuntimeStatusPort;

import com.klsjnh.infrastructure.config.KrtSecurityConfig011;

import org.springframework.stereotype.Component;

/**
 * Exposes the krt.status gate decisions to the application layer (adapter over
 * KrtSecurityConfig011).
 */

@Component
public class RuntimeStatusAdapter implements RuntimeStatusPort {

    /**
     * Framework config.
     */
    private final KrtSecurityConfig011 krtConfig;

    /**
     * Create the adapter.
     *
     * @param krtConfig framework config
     */
    public RuntimeStatusAdapter(KrtSecurityConfig011 krtConfig) {
        this.krtConfig = krtConfig;
    }

    /**
     * Whether passwordless login is permitted by the current runtime mode.
     *
     * @return true for debug / development
     */
    @Override
    public boolean allowsPasswordlessLogin() {
        return krtConfig.getStatus().allowsPasswordlessLogin();
    }

    /**
     * Whether the current runtime mode is debug.
     *
     * @return true for {@code debug}
     */
    @Override
    public boolean isDebug() {
        return krtConfig.getStatus().isDebug();
    }
}