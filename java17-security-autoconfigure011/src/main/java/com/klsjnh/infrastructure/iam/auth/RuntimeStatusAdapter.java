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
 *      2026.09.26  whitelist mode requires access center marker
 *
 */

import com.klsjnh.domain.iam.auth.AccessCenterMarker011;
import com.klsjnh.domain.iam.auth.RuntimeStatusPort;

import com.klsjnh.infrastructure.config.KrtSecurityConfig011;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Exposes the krt.status gate decisions to the application layer (adapter over
 * KrtSecurityConfig011). Production permission whitelist mode is active only
 * when the access center marker is present.
 */

@Component
public class RuntimeStatusAdapter implements RuntimeStatusPort {

    /**
     * Framework config.
     */
    private final KrtSecurityConfig011 krtConfig;

    /**
     * Access center marker (empty when center-access011-starter is absent).
     */
    private final ObjectProvider<AccessCenterMarker011> accessCenter;

    /**
     * Create the adapter.
     *
     * @param krtConfig    framework config
     * @param accessCenter access center marker provider
     */
    public RuntimeStatusAdapter(KrtSecurityConfig011 krtConfig,
            ObjectProvider<AccessCenterMarker011> accessCenter) {
        this.krtConfig = krtConfig;
        this.accessCenter = accessCenter;
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

    /**
     * Whether permission PEP is whitelist (production) and the access center
     * is on the classpath. Without the access center, whitelist stays off so
     * user-only assemblies are not forced to call assertHas.
     *
     * @return true when production and access center present
     */
    @Override
    public boolean isPermissionWhitelistMode() {
        return accessCenter.getIfAvailable() != null && krtConfig.getStatus().isPermissionWhitelistMode();
    }
}
