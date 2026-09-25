package com.klsjnh.infrastructure.iam.auth;

/*                EmptyUserRoleCodesPort class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  empty role codes when access center absent
 *
 */

import com.klsjnh.domain.iam.auth.AccessCenterMarker011;
import com.klsjnh.domain.iam.auth.UserRoleCodesPort;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fallback {@link UserRoleCodesPort} when the access center is absent: login
 * returns no role codes.
 */

@Component
@ConditionalOnMissingBean(AccessCenterMarker011.class)
public class EmptyUserRoleCodesPort implements UserRoleCodesPort {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> findRoleCodes(String userId) {
        return List.of();
    }
}
