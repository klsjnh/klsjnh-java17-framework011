package com.klsjnh.infrastructure.iam.auth;

/*                UserRoleCodesAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  role codes for login via access center
 *
 */

import com.klsjnh.domain.iam.auth.UserRoleCodesPort;
import com.klsjnh.domain.iam.role.JulyRoleRepository;
import com.klsjnh.domain.iam.role.JulyUserRoleRepository;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Access-center implementation of {@link UserRoleCodesPort}.
 */

@Component
public class UserRoleCodesAdapter implements UserRoleCodesPort {

    /**
     * User-role junction.
     */
    private final JulyUserRoleRepository userRoleRepository;

    /**
     * Role repository.
     */
    private final JulyRoleRepository roleRepository;

    /**
     * Create the adapter.
     *
     * @param userRoleRepository user-role junction
     * @param roleRepository     role repository
     */
    public UserRoleCodesAdapter(JulyUserRoleRepository userRoleRepository, JulyRoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> findRoleCodes(String userId) {
        if (userId == null || userId.isBlank()) {
            return List.of();
        }

        return roleRepository.findCodesByIds(userRoleRepository.findRoleIds(userId));
    }
}
