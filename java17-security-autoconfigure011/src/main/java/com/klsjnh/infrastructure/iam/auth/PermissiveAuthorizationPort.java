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

import com.klsjnh.domain.iam.auth.AccessCenterMarker011;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.auth.PermissionCheckContext011;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Fallback {@link AuthorizationPort} when {@code center-access011-starter} is
 * not on the classpath: every check succeeds. The production whitelist gate
 * remains off without the access-center marker, so marking here is only for
 * callers that still invoke {@code assertHas}.
 */

@Component
@ConditionalOnMissingBean(AccessCenterMarker011.class)
public class PermissiveAuthorizationPort implements AuthorizationPort {

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
