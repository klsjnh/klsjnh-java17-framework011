package com.klsjnh.application.iam.role;

/*                JulyRoleUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role use case class
 *      2026.09.15  update accepts status
 *      2026.09.26  explicit permission checks (julyRole auth)
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.perm.JulyPermAction;
import com.klsjnh.domain.iam.perm.JulyPermActionRepository;
import com.klsjnh.domain.iam.perm.JulyPermObjectRepository;
import com.klsjnh.domain.iam.role.JulyRole;
import com.klsjnh.domain.iam.role.JulyRolePermissionCodes011;
import com.klsjnh.domain.iam.role.JulyRoleRepository;
import com.klsjnh.domain.iam.user.JulyUser;
import com.klsjnh.domain.iam.user.JulyUserRepository;
import com.klsjnh.domain.iam.role.JulyUserRoleRepository;
import com.klsjnh.domain.iam.menu.JulyMenu;
import com.klsjnh.domain.iam.menu.JulyMenuRepository;
import com.klsjnh.domain.iam.role.JulyRolePermissionsRepository;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.application.platform011.backup.BackupUseCase;
import com.klsjnh.application.platform011.export.ExportUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JulyRole use cases: role CRUD with built-in role protection. Management
 * actions assert permission codes via {@link AuthorizationPort}.
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
     * Permission object catalog.
     */
    private final JulyPermObjectRepository permObjectRepository;

    /**
     * Permission action catalog.
     */
    private final JulyPermActionRepository permActionRepository;

    /**
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * Platform export use case.
     */
    private final ExportUseCase exportUseCase;

    /**
     * Platform backup use case.
     */
    private final BackupUseCase backupUseCase;

    /**
     * Create the use case.
     *
     * @param repository                july role repository
     * @param menuRepository            july menu repository
     * @param rolePermissionsRepository role permissions junction repository
     * @param userRoleRepository        user role junction repository
     * @param userRepository            july user repository
     * @param permObjectRepository      permission object catalog
     * @param permActionRepository      permission action catalog
     * @param authorizationPort         authorization port
     * @param exportUseCase             export use case
     * @param backupUseCase             backup use case
     */
    public JulyRoleUseCase(JulyRoleRepository repository, JulyMenuRepository menuRepository,
            JulyRolePermissionsRepository rolePermissionsRepository, JulyUserRoleRepository userRoleRepository,
            JulyUserRepository userRepository, JulyPermObjectRepository permObjectRepository,
            JulyPermActionRepository permActionRepository, AuthorizationPort authorizationPort,
            ExportUseCase exportUseCase, BackupUseCase backupUseCase) {
        this.repository = repository;
        this.menuRepository = menuRepository;
        this.rolePermissionsRepository = rolePermissionsRepository;
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.permObjectRepository = permObjectRepository;
        this.permActionRepository = permActionRepository;
        this.authorizationPort = authorizationPort;
        this.exportUseCase = exportUseCase;
        this.backupUseCase = backupUseCase;
    }

    /**
     * Assign menus to a role (toggle semantics, replace strategy): granted
     * menus revive their permission rows, revoked menus stop theirs. The row
     * records the menu's current permission code as a snapshot (blank for
     * pure visibility).
     *
     * @param operatorId operator user id
     * @param id         role id
     * @param pkMenus    menu ids to grant
     */
    @Transactional
    public void assignMenus(String operatorId, String id, List<String> pkMenus) {
        authorizationPort.assertHas(operatorId, JulyRolePermissionCodes011.ASSIGN_MENUS);
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
     * Assign permission actions of one catalog object to a role (replace
     * strategy within that object): desired action codes are granted by
     * permission_code with blank pk_menu; other catalog codes of the same
     * object that the role currently holds are revoked (menu-channel rows with
     * the same code are left untouched).
     *
     * @param operatorId  operator user id
     * @param id          role id
     * @param objectCode  catalog object code (e.g. julyScheduler)
     * @param actionCodes action codes to keep under that object
     */
    @Transactional
    public void assignObjectActions(String operatorId, String id, String objectCode, List<String> actionCodes) {
        authorizationPort.assertHas(operatorId, JulyRolePermissionCodes011.ASSIGN_OBJECT_ACTIONS);
        require(id);

        if (StringUtil011.isBlank(objectCode)) {
            throw BusinessException.badRequest("objectCode is required");
        }

        String object = objectCode.trim();

        if (permObjectRepository.findByCode(object) == null) {
            throw BusinessException.badRequest("permission object not found: " + object);
        }

        List<JulyPermAction> catalogActions = permActionRepository.findEnabledByObjectCode(object);
        Set<String> catalogCodes = catalogActions.stream().map(JulyPermAction::permissionCode)
                .collect(Collectors.toCollection(HashSet::new));

        Set<String> desiredActions = actionCodes == null ? Set.of()
                : actionCodes.stream().filter(s -> s != null && !s.isBlank()).map(String::trim)
                        .collect(Collectors.toCollection(HashSet::new));

        Set<String> desiredCodes = new HashSet<>();

        for (String actionCode : desiredActions) {
            JulyPermAction action = permActionRepository.findByObjectAndAction(object, actionCode);

            if (action == null) {
                throw BusinessException.badRequest(
                        "permission action not found: object=" + object + ", action=" + actionCode);
            }

            desiredCodes.add(action.permissionCode());
            rolePermissionsRepository.grantByCode(id, action.permissionCode());
        }

        for (String held : rolePermissionsRepository.findPermissionCodes(id)) {
            if (catalogCodes.contains(held) && !desiredCodes.contains(held)) {
                rolePermissionsRepository.revokeByCode(id, held);
            }
        }
    }

    /**
     * Permission codes currently held by a role (menu + direct grants).
     *
     * @param operatorId operator user id
     * @param id         role id
     * @return permission codes
     */
    public List<String> listPermissionCodes(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyRolePermissionCodes011.SELECT);
        require(id);

        return rolePermissionsRepository.findPermissionCodes(id);
    }

    /**
     * Insert a new custom role.
     *
     * @param operatorId operator user id
     * @param roleCode   role code, unique
     * @param roleName   role name
     * @param remark     remark
     * @return new role id
     */
    @Transactional
    public String insert(String operatorId, String roleCode, String roleName, String remark) {
        authorizationPort.assertHas(operatorId, JulyRolePermissionCodes011.INSERT);

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
     * @param operatorId operator user id
     * @param id         role id
     * @param roleName   role name
     * @param remark     remark
     * @param status     role status ("1" / "0"), nullable
     * @return role id
     */
    @Transactional
    public String update(String operatorId, String id, String roleName, String remark, String status) {
        authorizationPort.assertHas(operatorId, JulyRolePermissionCodes011.UPDATE);
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
     * @param operatorId operator user id
     * @param id         role id
     * @return deleted role id
     */
    @Transactional
    public String logicDelete(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyRolePermissionCodes011.LOGIC_DELETE);
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
     * @param operatorId operator user id
     * @param id         role id
     * @return aggregate
     */
    public JulyRole getById(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyRolePermissionCodes011.SELECT);
        return require(id);
    }

    /**
     * Menus currently granted to a role, returned as a flat list (no children
     * assembly) so the caller can diff it against the menu tree by id.
     *
     * @param operatorId operator user id
     * @param id         role id
     * @return granted menu aggregates, empty when nothing is granted
     */
    public List<JulyMenu> getMenusByRole(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyRolePermissionCodes011.SELECT);
        require(id);

        return menuRepository.findByIds(rolePermissionsRepository.findMenuIds(id));
    }

    /**
     * Users currently holding a role, returned as full aggregates so the
     * caller can re-submit the complete role set to {@code assignRoles}
     * without losing the roles granted elsewhere.
     *
     * @param operatorId operator user id
     * @param id         role id
     * @return user aggregates, empty when the role is held by nobody
     */
    public List<JulyUser> getUsersByRole(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyRolePermissionCodes011.SELECT);
        require(id);

        return userRepository.findByIds(userRoleRepository.findUserIds(id));
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param operatorId operator user id
     * @param pageQuery  page query, null falls back to page 1 / size 10
     * @param keyword    role code / name keyword, nullable
     * @return page result
     */
    public PageResult011<JulyRole> selectListByPage(String operatorId, PageQuery011 pageQuery, String keyword) {
        authorizationPort.assertHas(operatorId, JulyRolePermissionCodes011.SELECT);
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        List<JulyRole> rows = repository.findPage(query.offset(), query.pageSize(), keyword);
        long total = repository.count(keyword);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Export all role rows (permission-gated; payload is export result only).
     *
     * @param operator authenticated operator
     * @return export result
     */
    public ExportResult export(Operator011 operator) {
        requireOperator(operator);
        authorizationPort.assertHas(operator.id(), JulyRolePermissionCodes011.EXPORT);
        return exportUseCase.export(AuditObjectCodes011.JULY_ROLE, operator);
    }

    /**
     * Backup all role rows to object storage (permission-gated). Returns the
     * storage object key only — never the role row payload.
     *
     * @param operator authenticated operator
     * @return storage object key
     */
    public String backup(Operator011 operator) {
        requireOperator(operator);
        authorizationPort.assertHas(operator.id(), JulyRolePermissionCodes011.BACKUP);
        return backupUseCase.backup(AuditObjectCodes011.JULY_ROLE, operator);
    }

    /**
     * Require an authenticated operator.
     *
     * @param operator operator
     */
    private void requireOperator(Operator011 operator) {
        if (operator == null || !operator.authenticated()) {
            throw BusinessException.unauthorized("not authenticated");
        }
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
