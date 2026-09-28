package com.klsjnh.infrastructure.iam.auth;

/*                PermissiveAuthorizationPort class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  fallback AuthorizationPort when access center absent
 *
 */

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.auth.PermissionCheckContext011;
import com.klsjnh.domain.iam.auth.RuntimeStatusPort;

import java.util.Set;

/**
 * Fallback {@link AuthorizationPort} when {@code center-access011-starter} is
 * not on the classpath: every check succeeds.
 * <p>
 * <b>Explicit opt-in, never fail-open.</b> This class is not a Spring component
 * — it is created only by {@code AuthFallbackAutoConfiguration011}, which runs
 * after the access center, registers the port solely when no other
 * {@link AuthorizationPort} exists, and requires the explicit
 * {@code krt.security.permissive=true} opt-in. Without the access center and
 * without the opt-in no port exists at all and the context fails to start (the
 * framework's own use cases inject it as a required dependency), and the
 * constructor below refuses any non-debug runtime.
 * </p>
 */

public class PermissiveAuthorizationPort implements AuthorizationPort {

    /**
     * Create the fallback port; refuse to start outside debug.
     *
     * @param runtimeStatusPort runtime status port
     */
    public PermissiveAuthorizationPort(RuntimeStatusPort runtimeStatusPort) {
        if (!runtimeStatusPort.isDebug()) {
            throw new IllegalStateException("krt.security.permissive=true is debug-only; "
                    + "add center-access011-starter instead of running without authorization");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> listCodes(String operatorId) {
        return Set.of("*");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(String operatorId, String permissionCode) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertHas(String operatorId, String permissionCode) {
        PermissionCheckContext011.markChecked();
    }
}
