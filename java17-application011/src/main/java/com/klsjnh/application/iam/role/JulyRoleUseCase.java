package com.klsjnh.application.iam.role;

/*                JulyRoleUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.15
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role use case class
 *      2026.09.15  update accepts status
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.iam.role.JulyRole;
import com.klsjnh.domain.iam.role.JulyRoleRepository;
import com.klsjnh.domain.iam.user.JulyUser;
import com.klsjnh.domain.iam.user.JulyUserRepository;
import com.klsjnh.domain.iam.role.JulyUserRoleRepository;
import com.klsjnh.domain.system011.menu.JulyMenu;
import com.klsjnh.domain.system011.menu.JulyMenuRepository;
import com.klsjnh.domain.system011.menu.JulyRolePermissionsRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JulyRole use cases: role CRUD with built-in role protection.
 */

@Service
public class JulyRoleUseCase {

    /**
     * JulyRole repository.
     */
    private final JulyRoleRepository repository;

    /**
     * JulyMenu repository (menu existence + permission code lookup).
     */
    private final JulyMenuRepository menuRepository;

    /**
     * Role permissions junction repository.
     */
    private final JulyRolePermissionsRepository rolePermissionsRepository;

    /**
     * User role junction repository (reverse lookup: role → holding users).
     */
    private final JulyUserRoleRepository userRoleRepository;

    /**
     * JulyUser repository (user id → full aggregate).
     */
    private final JulyUserRepository userRepository;

    /**
     * Create the use case.
     *
     * @param repository                july role repository
     * @param menuRepository            july menu repository
     * @param rolePermissionsRepository role permissions junction repository
     * @param userRoleRepository        user role junction repository
     * @param userRepository            july user repository
     */
    public JulyRoleUseCase(JulyRoleRepository repository, JulyMenuRepository menuRepository,
            JulyRolePermissionsRepository rolePermissionsRepository, JulyUserRoleRepository userRoleRepository,
            JulyUserRepository userRepository) {
        this.repository = repository;
        this.menuRepository = menuRepository;
        this.rolePermissionsRepository = rolePermissionsRepository;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
    }

    /**
     * Assign menus to a role (toggle semantics, replace strategy): granted
     * menus revive their permission rows, revoked menus stop theirs. The row
     * records the menu's current permission code as a snapshot (blank for
     * pure visibility).
     *
     * @param id      role id
     * @param pkMenus menu ids to grant
     */
    @Transactional
    public void assignMenus(String id, List<String> pkMenus) {
        require(id);

        List<String> desired = pkMenus == null ? List.of()
                : pkMenus.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).distinct().toList();
        List<String> current = rolePermissionsRepository.findMenuIds(id);

        for (String pkMenu : desired) {
            JulyMenu menu = menuRepository.findById(pkMenu);

            if (menu == null) {
                throw BusinessException.badRequest("menu not found, id=" + pkMenu);
            }

            String code = menu.permissionCode() == null ? "" : menu.permissionCode();
            rolePermissionsRepository.grant(id, pkMenu, code);
        }

        for (String pkMenu : current) {
            if (!desired.contains(pkMenu)) {
                rolePermissionsRepository.revokeByMenu(id, pkMenu);
            }
        }
    }

    /**
     * Insert a new custom role.
     *
     * @param roleCode role code, unique
     * @param roleName role name
     * @param remark   remark
     * @return new role id
     */
    @Transactional
    public String insert(String roleCode, String roleName, String remark) {
        if (repository.findByCode(roleCode) != null) {
            throw BusinessException.badRequest("role code already exists: " + roleCode);
        }

        JulyRole role = JulyRole.create(EntityId.generate(), roleCode, roleName, remark, AuditInfo.empty());
        repository.insert(role);

        return role.id().value();
    }

    /**
     * Update a role (built-in roles allow name / remark changes). Status is
     * optional: a null / blank value leaves the current status untouched.
     *
     * @param id       role id
     * @param roleName role name
     * @param remark   remark
     * @param status   role status ("1" / "0"), nullable
     * @return role id
     */
    @Transactional
    public String update(String id, String roleName, String remark, String status) {
        JulyRole role = require(id);
        role.updateBasics(roleName, remark);

        if (status != null && !status.isBlank()) {
            role.changeStatus(status);
        }

        repository.update(role);

        return role.id().value();
    }

    /**
     * Logic delete a role; built-in roles are protected.
     *
     * @param id role id
     * @return deleted role id
     */
    @Transactional
    public String logicDelete(String id) {
        JulyRole role = require(id);

        if ("1".equals(role.isBuiltin())) {
            throw BusinessException.badRequest("built-in role cannot be deleted");
        }

        repository.logicDeleteById(id);

        return role.id().value();
    }

    /**
     * Find by primary key.
     *
     * @param id role id
     * @return aggregate
     */
    public JulyRole getById(String id) {
        return require(id);
    }

    /**
     * Menus currently granted to a role, returned as a flat list (no children
     * assembly) so the caller can diff it against the menu tree by id.
     *
     * @param id role id
     * @return granted menu aggregates, empty when nothing is granted
     */
    public List<JulyMenu> getMenusByRole(String id) {
        require(id);

        return menuRepository.findByIds(rolePermissionsRepository.findMenuIds(id));
    }

    /**
     * Users currently holding a role, returned as full aggregates so the
     * caller can re-submit the complete role set to {@code assignRoles}
     * without losing the roles granted elsewhere.
     *
     * @param id role id
     * @return user aggregates, empty when the role is held by nobody
     */
    public List<JulyUser> getUsersByRole(String id) {
        require(id);

        return userRepository.findByIds(userRoleRepository.findUserIds(id));
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param keyword   role code / name keyword, nullable
     * @return page result
     */
    public PageResult011<JulyRole> selectListByPage(PageQuery011 pageQuery, String keyword) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        List<JulyRole> rows = repository.findPage(query.offset(), query.pageSize(), keyword);
        long total = repository.count(keyword);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Require an existing role.
     *
     * @param id role id
     * @return aggregate
     */
    private JulyRole require(String id) {
        JulyRole role = repository.findById(id);

        if (role == null) {
            throw BusinessException.recordNotFound(id);
        }

        return role;
    }
}
