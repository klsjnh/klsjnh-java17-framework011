package com.klsjnh.infrastructure.iam.auth.seed;

/*                AuthPermissionDemoSeed011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate 2026.09.24
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  seed julyConfig permission demo roles and users
 *      2026.09.24  seed perm catalog + julyScheduler start-only demo
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.constant.AuditObjectCodes011;

import com.klsjnh.domain.iam.auth.PasswordPort;
import com.klsjnh.domain.iam.perm.JulyPermAction;
import com.klsjnh.domain.iam.perm.JulyPermActionRepository;
import com.klsjnh.domain.iam.perm.JulyPermObject;
import com.klsjnh.domain.iam.perm.JulyPermObjectRepository;
import com.klsjnh.domain.iam.role.JulyRole;
import com.klsjnh.domain.iam.role.JulyRoleRepository;
import com.klsjnh.domain.iam.user.JulyUser;
import com.klsjnh.domain.iam.user.JulyUserRepository;
import com.klsjnh.domain.iam.role.JulyUserRoleRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.config.JulyConfig;
import com.klsjnh.domain.system011.config.JulyConfigPermissionCodes011;
import com.klsjnh.domain.system011.config.JulyConfigRepository;
import com.klsjnh.domain.iam.menu.JulyMenu;
import com.klsjnh.domain.iam.menu.JulyMenuRepository;
import com.klsjnh.domain.iam.role.JulyRolePermissionsRepository;
import com.klsjnh.domain.system011.scheduler.JulyScheduler;
import com.klsjnh.domain.system011.scheduler.JulySchedulerPermissionCodes011;
import com.klsjnh.domain.system011.scheduler.JulySchedulerRepository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Idempotent seed for permission demos: julyConfig menu matrix, permission
 * catalog (julyConfig / julyScheduler), start-only role {@code test017}, and a
 * sample scheduler task.
 */

@Component
public class AuthPermissionDemoSeed011 {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(AuthPermissionDemoSeed011.class);

    /**
     * Shared demo password (bcrypt at insert).
     */
    public static final String DEMO_PASSWORD = "Test011!";

    /**
     * Menu repository.
     */
    private final JulyMenuRepository menuRepository;

    /**
     * Role repository.
     */
    private final JulyRoleRepository roleRepository;

    /**
     * Role permissions.
     */
    private final JulyRolePermissionsRepository rolePermissionsRepository;

    /**
     * User repository.
     */
    private final JulyUserRepository userRepository;

    /**
     * User roles.
     */
    private final JulyUserRoleRepository userRoleRepository;

    /**
     * Config repository.
     */
    private final JulyConfigRepository configRepository;

    /**
     * Permission object catalog.
     */
    private final JulyPermObjectRepository permObjectRepository;

    /**
     * Permission action catalog.
     */
    private final JulyPermActionRepository permActionRepository;

    /**
     * Scheduler repository.
     */
    private final JulySchedulerRepository schedulerRepository;

    /**
     * Password port.
     */
    private final PasswordPort passwordPort;

    /**
     * Create the seed.
     *
     * @param menuRepository             menu repository
     * @param roleRepository             role repository
     * @param rolePermissionsRepository  role permissions
     * @param userRepository             user repository
     * @param userRoleRepository         user roles
     * @param configRepository           config repository
     * @param permObjectRepository       permission object catalog
     * @param permActionRepository       permission action catalog
     * @param schedulerRepository        scheduler repository
     * @param passwordPort               password port
     */
    public AuthPermissionDemoSeed011(JulyMenuRepository menuRepository, JulyRoleRepository roleRepository,
            JulyRolePermissionsRepository rolePermissionsRepository, JulyUserRepository userRepository,
            JulyUserRoleRepository userRoleRepository, JulyConfigRepository configRepository,
            JulyPermObjectRepository permObjectRepository, JulyPermActionRepository permActionRepository,
            JulySchedulerRepository schedulerRepository, PasswordPort passwordPort) {
        this.menuRepository = menuRepository;
        this.roleRepository = roleRepository;
        this.rolePermissionsRepository = rolePermissionsRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.configRepository = configRepository;
        this.permObjectRepository = permObjectRepository;
        this.permActionRepository = permActionRepository;
        this.schedulerRepository = schedulerRepository;
        this.passwordPort = passwordPort;
    }

    /**
     * Seed demo menus, roles, users, catalog and a sample scheduler when missing.
     */
    public void seed() {
        String funcName = "auth permission demo seed";

        seedJulyConfigMenuDemo();
        seedPermissionCatalog();
        seedSchedulerStartOnlyDemo();

        logger.info("{} ready (accounts klsjnh/test011/test013/test015/test016/test017 password {})", funcName,
                DEMO_PASSWORD);
    }

    /**
     * julyConfig menu × role matrix (033 §021).
     */
    private void seedJulyConfigMenuDemo() {
        String dirId = ensureMenu("demo_sys_dir", "系统管理(演示)", "1", null, "", null, "", 10);
        String pageId = ensureMenu("demo_july_config", "配置管理(演示)", "2", dirId, "/system011/julyConfig", null,
                "JulyConfig", 20);

        Map<String, String> buttonIds = new LinkedHashMap<>();
        buttonIds.put(JulyConfigPermissionCodes011.SELECT,
                ensureMenu("demo_jc_select", "查看", "3", pageId, null, JulyConfigPermissionCodes011.SELECT, null, 1));
        buttonIds.put(JulyConfigPermissionCodes011.INSERT,
                ensureMenu("demo_jc_insert", "新增", "3", pageId, null, JulyConfigPermissionCodes011.INSERT, null, 2));
        buttonIds.put(JulyConfigPermissionCodes011.UPDATE,
                ensureMenu("demo_jc_update", "修改", "3", pageId, null, JulyConfigPermissionCodes011.UPDATE, null, 3));
        buttonIds.put(JulyConfigPermissionCodes011.LOGIC_DELETE, ensureMenu("demo_jc_delete", "删除", "3", pageId, null,
                JulyConfigPermissionCodes011.LOGIC_DELETE, null, 4));
        buttonIds.put(JulyConfigPermissionCodes011.EXPORT,
                ensureMenu("demo_jc_export", "导出", "3", pageId, null, JulyConfigPermissionCodes011.EXPORT, null, 5));
        buttonIds.put(JulyConfigPermissionCodes011.BACKUP,
                ensureMenu("demo_jc_backup", "备份", "3", pageId, null, JulyConfigPermissionCodes011.BACKUP, null, 6));

        List<String> allButtonIds = new ArrayList<>(buttonIds.values());
        allButtonIds.add(0, pageId);
        allButtonIds.add(0, dirId);

        String roleSuper = ensureRole("role_super_admin", "超级管理员", true, "033 demo builtin");
        String roleAdmin = ensureRole("role_admin", "管理员", false, "no export/backup");
        String roleSuperUser = ensureRole("role_super_user", "超级用户", false, "no delete");
        String roleUser = ensureRole("role_user", "普通用户", false, "select only");
        String roleBackup = ensureRole("role_backup_admin", "备份管理员", false, "backup only");

        grantMenus(roleSuper, allButtonIds);
        grantMenus(roleAdmin, List.of(dirId, pageId, buttonIds.get(JulyConfigPermissionCodes011.SELECT),
                buttonIds.get(JulyConfigPermissionCodes011.INSERT), buttonIds.get(JulyConfigPermissionCodes011.UPDATE),
                buttonIds.get(JulyConfigPermissionCodes011.LOGIC_DELETE)));
        grantMenus(roleSuperUser, List.of(dirId, pageId, buttonIds.get(JulyConfigPermissionCodes011.SELECT),
                buttonIds.get(JulyConfigPermissionCodes011.INSERT), buttonIds.get(JulyConfigPermissionCodes011.UPDATE),
                buttonIds.get(JulyConfigPermissionCodes011.EXPORT), buttonIds.get(JulyConfigPermissionCodes011.BACKUP)));
        grantMenus(roleUser, List.of(dirId, pageId, buttonIds.get(JulyConfigPermissionCodes011.SELECT)));
        grantMenus(roleBackup, List.of(dirId, pageId, buttonIds.get(JulyConfigPermissionCodes011.BACKUP)));

        ensureUser("klsjnh", "超级管理员", roleSuper);
        ensureUser("test011", "管理员", roleAdmin);
        ensureUser("test013", "超级用户", roleSuperUser);
        ensureUser("test015", "普通用户", roleUser);
        ensureUser("test016", "备份管理员", roleBackup);

        if (configRepository.findEnabledByCode("demo.auth.probe") == null) {
            configRepository.insert(JulyConfig.create(EntityId.generate(), "demo.auth.probe", "seeded", null,
                    "033 permission demo row", AuditInfo.empty()));
        }
    }

    /**
     * Permission catalog for julyConfig + julyScheduler blueprints.
     */
    private void seedPermissionCatalog() {
        ensurePermObject(AuditObjectCodes011.JULY_CONFIG, "配置管理", "system011", 10);
        ensurePermAction(AuditObjectCodes011.JULY_CONFIG, "select", "查看", JulyConfigPermissionCodes011.SELECT, 1);
        ensurePermAction(AuditObjectCodes011.JULY_CONFIG, "insert", "新增", JulyConfigPermissionCodes011.INSERT, 2);
        ensurePermAction(AuditObjectCodes011.JULY_CONFIG, "update", "修改", JulyConfigPermissionCodes011.UPDATE, 3);
        ensurePermAction(AuditObjectCodes011.JULY_CONFIG, "logicDelete", "删除", JulyConfigPermissionCodes011.LOGIC_DELETE,
                4);
        ensurePermAction(AuditObjectCodes011.JULY_CONFIG, "export", "导出", JulyConfigPermissionCodes011.EXPORT, 5);
        ensurePermAction(AuditObjectCodes011.JULY_CONFIG, "backup", "备份", JulyConfigPermissionCodes011.BACKUP, 6);

        ensurePermObject(AuditObjectCodes011.JULY_SCHEDULER, "定时任务", "system011", 20);
        ensurePermAction(AuditObjectCodes011.JULY_SCHEDULER, "select", "查看", JulySchedulerPermissionCodes011.SELECT, 1);
        ensurePermAction(AuditObjectCodes011.JULY_SCHEDULER, "insert", "新增", JulySchedulerPermissionCodes011.INSERT, 2);
        ensurePermAction(AuditObjectCodes011.JULY_SCHEDULER, "update", "修改", JulySchedulerPermissionCodes011.UPDATE, 3);
        ensurePermAction(AuditObjectCodes011.JULY_SCHEDULER, "logicDelete", "删除",
                JulySchedulerPermissionCodes011.LOGIC_DELETE, 4);
        ensurePermAction(AuditObjectCodes011.JULY_SCHEDULER, "start", "启动", JulySchedulerPermissionCodes011.START, 5);
        ensurePermAction(AuditObjectCodes011.JULY_SCHEDULER, "stop", "停止", JulySchedulerPermissionCodes011.STOP, 6);
        ensurePermAction(AuditObjectCodes011.JULY_SCHEDULER, "executeOnce", "执行一次",
                JulySchedulerPermissionCodes011.EXECUTE_ONCE, 7);
    }

    /**
     * Start-only role + test017 + demo scheduler task.
     */
    private void seedSchedulerStartOnlyDemo() {
        String roleStart = ensureRole("role_scheduler_start", "调度启动员", false, "julyScheduler start+select only");

        rolePermissionsRepository.grantByCode(roleStart, JulySchedulerPermissionCodes011.SELECT);
        rolePermissionsRepository.grantByCode(roleStart, JulySchedulerPermissionCodes011.START);

        ensureUser("test017", "调度启动员", roleStart);

        if (schedulerRepository.findByCode("demo.auth.scheduler") == null) {
            schedulerRepository.insert(JulyScheduler.create(EntityId.generate(), "demo.auth.scheduler",
                    "权限演示任务", "demo011scheduler", "0 0 0 1 1 ?", "P3 dynamic auth probe", AuditInfo.empty()));
        }
    }

    /**
     * Ensure a permission object by code.
     *
     * @param objectCode object code
     * @param objectName name
     * @param moduleCode module
     * @param sortOrder  sort
     */
    private void ensurePermObject(String objectCode, String objectName, String moduleCode, int sortOrder) {
        if (permObjectRepository.findByCode(objectCode) != null) {
            return;
        }

        permObjectRepository.insert(JulyPermObject.create(EntityId.generate(), objectCode, objectName, moduleCode,
                sortOrder, null, AuditInfo.empty()));
    }

    /**
     * Ensure a permission action by permission code.
     *
     * @param objectCode     object code
     * @param actionCode     action code
     * @param actionName     name
     * @param permissionCode full code
     * @param sortOrder      sort
     */
    private void ensurePermAction(String objectCode, String actionCode, String actionName, String permissionCode,
            int sortOrder) {
        if (permActionRepository.findByPermissionCode(permissionCode) != null) {
            return;
        }

        if (permActionRepository.findByObjectAndAction(objectCode, actionCode) != null) {
            return;
        }

        permActionRepository.insert(JulyPermAction.create(EntityId.generate(), objectCode, actionCode, actionName,
                permissionCode, sortOrder, null, AuditInfo.empty()));
    }

    /**
     * Ensure a menu by code.
     *
     * @param menuCode       menu code
     * @param menuName       name
     * @param menuType       type
     * @param parentId       parent id
     * @param route          route
     * @param permissionCode permission code
     * @param component      component
     * @param sortOrder      sort
     * @return menu id
     */
    private String ensureMenu(String menuCode, String menuName, String menuType, String parentId, String route,
            String permissionCode, String component, int sortOrder) {
        JulyMenu existing = menuRepository.findByCode(menuCode);

        if (existing != null) {
            return existing.id().value();
        }

        JulyMenu menu = JulyMenu.create(EntityId.generate(), menuCode, menuName, menuType, null, route, permissionCode,
                component, parentId == null ? "" : parentId, sortOrder, AuditInfo.empty());
        menuRepository.insert(menu);

        return menu.id().value();
    }

    /**
     * Ensure a role by code.
     *
     * @param roleCode  role code
     * @param roleName  name
     * @param builtin   built-in flag
     * @param remark    remark
     * @return role id
     */
    private String ensureRole(String roleCode, String roleName, boolean builtin, String remark) {
        JulyRole existing = roleRepository.findByCode(roleCode);

        if (existing != null) {
            return existing.id().value();
        }

        JulyRole role = builtin
                ? JulyRole.createBuiltin(EntityId.generate(), roleCode, roleName, remark, AuditInfo.empty())
                : JulyRole.create(EntityId.generate(), roleCode, roleName, remark, AuditInfo.empty());
        roleRepository.insert(role);

        return role.id().value();
    }

    /**
     * Replace role menu grants with the given set.
     *
     * @param roleId  role id
     * @param menuIds menu ids
     */
    private void grantMenus(String roleId, List<String> menuIds) {
        List<String> current = rolePermissionsRepository.findMenuIds(roleId);

        for (String menuId : menuIds) {
            JulyMenu menu = menuRepository.findById(menuId);

            if (menu == null) {
                continue;
            }

            String code = menu.permissionCode() == null ? "" : menu.permissionCode();
            rolePermissionsRepository.grant(roleId, menuId, code);
        }

        for (String menuId : current) {
            if (!menuIds.contains(menuId)) {
                rolePermissionsRepository.revokeByMenu(roleId, menuId);
            }
        }
    }

    /**
     * Ensure a user and bind a single role.
     *
     * @param account  login account
     * @param userName display name
     * @param roleId   role id
     */
    private void ensureUser(String account, String userName, String roleId) {
        JulyUser user = userRepository.findByAccount(account);

        if (user == null) {
            user = JulyUser.create(EntityId.generate(), account, userName, passwordPort.encode(DEMO_PASSWORD),
                    AuditInfo.empty());
            userRepository.insert(user);
        } else {
            user.resetPassword(passwordPort.encode(DEMO_PASSWORD));
            userRepository.update(user);
        }

        String userId = user.id().value();
        List<String> current = userRoleRepository.findRoleIds(userId);

        if (!current.contains(roleId)) {
            userRoleRepository.assign(userId, roleId);
        }

        for (String existing : current) {
            if (!existing.equals(roleId)) {
                userRoleRepository.unassign(userId, existing);
            }
        }
    }
}
