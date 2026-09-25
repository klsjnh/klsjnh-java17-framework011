package com.klsjnh.application.iam.user;

/*                JulyUserRoleAssignUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  assignRoles moved from core user use case to access center
 *      2026.09.26  explicit permission checks (assignRoles auth)
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.role.JulyUserRoleRepository;
import com.klsjnh.domain.iam.user.JulyUser;
import com.klsjnh.domain.iam.user.JulyUserPermissionCodes011;
import com.klsjnh.domain.iam.user.JulyUserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Access-center use case: assign roles to a user (toggle / replace).
 */

@Service
public class JulyUserRoleAssignUseCase {

    /**
     * User repository (core).
     */
    private final JulyUserRepository userRepository;

    /**
     * User-role junction.
     */
    private final JulyUserRoleRepository userRoleRepository;

    /**
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * Create the use case.
     *
     * @param userRepository     user repository
     * @param userRoleRepository user-role junction
     * @param authorizationPort  authorization port
     */
    public JulyUserRoleAssignUseCase(JulyUserRepository userRepository,
            JulyUserRoleRepository userRoleRepository, AuthorizationPort authorizationPort) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.authorizationPort = authorizationPort;
    }

    /**
     * Assign roles to a user (toggle semantics: granted roles are revived,
     * revoked roles are stopped).
     *
     * @param operatorId operator user id
     * @param id         user id
     * @param pkRoles    role ids to grant
     */
    @Transactional
    public void assignRoles(String operatorId, String id, List<String> pkRoles) {
        authorizationPort.assertHas(operatorId, JulyUserPermissionCodes011.ASSIGN_ROLES);
        JulyUser user = userRepository.findById(id);

        if (user == null) {
            throw BusinessException.recordNotFound(id);
        }

        List<String> desired = pkRoles == null ? List.of()
                : pkRoles.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).distinct().toList();
        List<String> current = userRoleRepository.findRoleIds(id);

        for (String pkRole : desired) {
            if (!current.contains(pkRole)) {
                userRoleRepository.assign(id, pkRole);
            }
        }

        for (String pkRole : current) {
            if (!desired.contains(pkRole)) {
                userRoleRepository.unassign(id, pkRole);
            }
        }
    }
}
