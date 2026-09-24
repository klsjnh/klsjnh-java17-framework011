package com.klsjnh.infrastructure.iam.auth;

/*                AuthorizationAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  authorization adapter (user roles to permission codes)
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.role.JulyRole;
import com.klsjnh.domain.iam.role.JulyRoleRepository;
import com.klsjnh.domain.iam.role.JulyUserRoleRepository;
import com.klsjnh.domain.system011.menu.JulyRolePermissionsRepository;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Authorization adapter: user → roles → permission codes, with built-in role
 * full bypass (same rule as getUserMenuTree).
 */

@Component
public class AuthorizationAdapter implements AuthorizationPort {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationAdapter.class);

    /**
     * Sentinel meaning the operator holds every code (built-in role).
     */
    private static final String BUILTIN_ALL = "*";

    /**
     * User-role junction.
     */
    private final JulyUserRoleRepository userRoleRepository;

    /**
     * Role repository.
     */
    private final JulyRoleRepository roleRepository;

    /**
     * Role-permission junction.
     */
    private final JulyRolePermissionsRepository rolePermissionsRepository;

    /**
     * Create the adapter.
     *
     * @param userRoleRepository         user-role junction
     * @param roleRepository             role repository
     * @param rolePermissionsRepository  role-permission junction
     */
    public AuthorizationAdapter(JulyUserRoleRepository userRoleRepository, JulyRoleRepository roleRepository,
            JulyRolePermissionsRepository rolePermissionsRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.rolePermissionsRepository = rolePermissionsRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> listCodes(String operatorId) {
        String funcName = "list codes";

        if (StringUtil011.isBlank(operatorId)) {
            return Set.of();
        }

        List<String> roleIds = userRoleRepository.findRoleIds(operatorId);

        if (roleIds.isEmpty()) {
            return Set.of();
        }

        Set<String> codes = new HashSet<>();

        for (String roleId : roleIds) {
            JulyRole role = roleRepository.findById(roleId);

            if (role != null && "1".equals(role.isBuiltin())) {
                logger.debug("{} operator {} built-in role bypass", funcName, operatorId);
                return Set.of(BUILTIN_ALL);
            }

            if (role == null) {
                continue;
            }

            codes.addAll(rolePermissionsRepository.findPermissionCodes(roleId));
        }

        codes.removeIf(StringUtil011::isBlank);

        return Collections.unmodifiableSet(codes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean has(String operatorId, String permissionCode) {
        if (StringUtil011.isBlank(permissionCode)) {
            return false;
        }

        Set<String> codes = listCodes(operatorId);

        return codes.contains(BUILTIN_ALL) || codes.contains(permissionCode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assertHas(String operatorId, String permissionCode) {
        String funcName = "assert has";

        if (StringUtil011.isBlank(operatorId)) {
            throw BusinessException.unauthorized("not authenticated");
        }

        if (!has(operatorId, permissionCode)) {
            logger.info("{} operator {} missing {}", funcName, operatorId, permissionCode);
            throw BusinessException.forbidden("forbidden: missing permission " + permissionCode);
        }
    }
}
