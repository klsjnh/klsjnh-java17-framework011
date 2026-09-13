package com.klsjnh.application.iam;

/*                JulyRoleUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role use case class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.iam.JulyRole;
import com.klsjnh.domain.iam.JulyRoleRepository;
import com.klsjnh.domain.menu.JulyMenu;
import com.klsjnh.domain.menu.JulyMenuRepository;
import com.klsjnh.domain.menu.JulyRolePermissionsRepository;
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
     * Create the use case.
     *
     * @param repository                 july role repository
     * @param menuRepository             july menu repository
     * @param rolePermissionsRepository  role permissions junction repository
     */
    public JulyRoleUseCase(JulyRoleRepository repository, JulyMenuRepository menuRepository,
            JulyRolePermissionsRepository rolePermissionsRepository) {
        this.repository = repository;
        this.menuRepository = menuRepository;
        this.rolePermissionsRepository = rolePermissionsRepository;
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
     * Update a role (built-in roles allow name / remark changes).
     *
     * @param id       role id
     * @param roleName role name
     * @param remark   remark
     * @return role id
     */
    @Transactional
    public String update(String id, String roleName, String remark) {
        JulyRole role = require(id);
        role.updateBasics(roleName, remark);
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
