package com.klsjnh.infrastructure.iam.perm.seed;

/*                JulyPermCatalogSeed011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  seed julyConfig / julyScheduler permission catalog
 *      2026.09.26  julyScheduler catalog seed moved to scheduler starter
 *      2026.09.26  seed IAM permission catalog (user / role / menu / org)
 *      2026.09.26  seed dictionary / datasource / sync / sql catalog
 *      2026.09.26  julyConfig / julyDictionary catalog seed moved to
 *                  platform ConditionalOnClass seed
 *      2026.09.26  datasource / sync / sql catalog seed moved to
 *                  DatasourcePermCatalogSeed011 (ConditionalOnClass)
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.domain.iam.menu.JulyMenuPermissionCodes011;
import com.klsjnh.domain.iam.organization.JulyOrganizationPermissionCodes011;
import com.klsjnh.domain.iam.perm.JulyPermAction;
import com.klsjnh.domain.iam.perm.JulyPermActionRepository;
import com.klsjnh.domain.iam.perm.JulyPermObject;
import com.klsjnh.domain.iam.perm.JulyPermObjectRepository;
import com.klsjnh.domain.iam.role.JulyRolePermissionCodes011;
import com.klsjnh.domain.iam.user.JulyUserAuditPermissionCodes011;
import com.klsjnh.domain.iam.user.JulyUserPermissionCodes011;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Component;

/**
 * Idempotent seed for the IAM permission catalog blueprint (codes must match
 * UseCase assertHas constants). julyConfig / julyDictionary rows are seeded by
 * {@code JulyPlatformPermCatalogSeed011} when the platform starter is present
 * ({@code @ConditionalOnBean(JulyPermObjectRepository)}); julyScheduler by the
 * quartz starter seed; datasource / sync / sql by
 * {@code DatasourcePermCatalogSeed011} when the datasource center is present.
 * Access only seeds IAM itself (user / role / menu / org / user-audit).
 */

@Component
public class JulyPermCatalogSeed011 {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(JulyPermCatalogSeed011.class);

    /**
     * Object catalog.
     */
    private final JulyPermObjectRepository objectRepository;

    /**
     * Action catalog.
     */
    private final JulyPermActionRepository actionRepository;

    /**
     * Create the seed.
     *
     * @param objectRepository object catalog
     * @param actionRepository action catalog
     */
    public JulyPermCatalogSeed011(JulyPermObjectRepository objectRepository,
            JulyPermActionRepository actionRepository) {
        this.objectRepository = objectRepository;
        this.actionRepository = actionRepository;
    }

    /**
     * Seed catalog rows when missing.
     */
    public void seed() {
        String funcName = "perm catalog seed";

        ensureObject("julyUser", "用户管理", "iam", 1);
        ensureAction("julyUser", "select", "查看", JulyUserPermissionCodes011.SELECT, 1);
        ensureAction("julyUser", "insert", "新增", JulyUserPermissionCodes011.INSERT, 2);
        ensureAction("julyUser", "update", "修改", JulyUserPermissionCodes011.UPDATE, 3);
        ensureAction("julyUser", "logicDelete", "删除", JulyUserPermissionCodes011.LOGIC_DELETE, 4);
        ensureAction("julyUser", "resetPassword", "重置密码", JulyUserPermissionCodes011.RESET_PASSWORD, 5);
        ensureAction("julyUser", "changePassword", "修改密码", JulyUserPermissionCodes011.CHANGE_PASSWORD, 6);
        ensureAction("julyUser", "assignRoles", "分配角色", JulyUserPermissionCodes011.ASSIGN_ROLES, 7);
        ensureAction("julyUser", "export", "导出", JulyUserPermissionCodes011.EXPORT, 8);
        ensureAction("julyUser", "backup", "备份", JulyUserPermissionCodes011.BACKUP, 9);

        ensureObject("julyUserAudit", "用户审计", "iam", 2);
        ensureAction("julyUserAudit", "select", "查看", JulyUserAuditPermissionCodes011.SELECT, 1);

        ensureObject("julyRole", "角色管理", "iam", 3);
        ensureAction("julyRole", "select", "查看", JulyRolePermissionCodes011.SELECT, 1);
        ensureAction("julyRole", "insert", "新增", JulyRolePermissionCodes011.INSERT, 2);
        ensureAction("julyRole", "update", "修改", JulyRolePermissionCodes011.UPDATE, 3);
        ensureAction("julyRole", "logicDelete", "删除", JulyRolePermissionCodes011.LOGIC_DELETE, 4);
        ensureAction("julyRole", "assignMenus", "授权菜单", JulyRolePermissionCodes011.ASSIGN_MENUS, 5);
        ensureAction("julyRole", "assignObjectActions", "授权动作", JulyRolePermissionCodes011.ASSIGN_OBJECT_ACTIONS, 6);
        ensureAction("julyRole", "export", "导出", JulyRolePermissionCodes011.EXPORT, 7);
        ensureAction("julyRole", "backup", "备份", JulyRolePermissionCodes011.BACKUP, 8);

        ensureObject("julyMenu", "菜单管理", "iam", 4);
        ensureAction("julyMenu", "select", "查看", JulyMenuPermissionCodes011.SELECT, 1);
        ensureAction("julyMenu", "insert", "新增", JulyMenuPermissionCodes011.INSERT, 2);
        ensureAction("julyMenu", "update", "修改", JulyMenuPermissionCodes011.UPDATE, 3);
        ensureAction("julyMenu", "logicDelete", "删除", JulyMenuPermissionCodes011.LOGIC_DELETE, 4);
        ensureAction("julyMenu", "export", "导出", JulyMenuPermissionCodes011.EXPORT, 5);
        ensureAction("julyMenu", "backup", "备份", JulyMenuPermissionCodes011.BACKUP, 6);

        ensureObject("julyOrganization", "组织机构", "iam", 5);
        ensureAction("julyOrganization", "select", "查看", JulyOrganizationPermissionCodes011.SELECT, 1);
        ensureAction("julyOrganization", "insert", "新增", JulyOrganizationPermissionCodes011.INSERT, 2);
        ensureAction("julyOrganization", "update", "修改", JulyOrganizationPermissionCodes011.UPDATE, 3);
        ensureAction("julyOrganization", "logicDelete", "删除", JulyOrganizationPermissionCodes011.LOGIC_DELETE, 4);
        ensureAction("julyOrganization", "export", "导出", JulyOrganizationPermissionCodes011.EXPORT, 5);
        ensureAction("julyOrganization", "backup", "备份", JulyOrganizationPermissionCodes011.BACKUP, 6);

        logger.info("{} ready (iam)", funcName);
    }

    /**
     * Ensure an object row exists.
     *
     * @param objectCode object code
     * @param objectName display name
     * @param moduleCode module
     * @param sortOrder  sort
     */
    private void ensureObject(String objectCode, String objectName, String moduleCode, int sortOrder) {
        if (objectRepository.findByCode(objectCode) != null) {
            return;
        }

        objectRepository.insert(JulyPermObject.create(EntityId.generate(), objectCode, objectName, moduleCode,
                sortOrder, null, AuditInfo.empty()));
    }

    /**
     * Ensure an action row exists.
     *
     * @param objectCode     object code
     * @param actionCode     action code
     * @param actionName     display name
     * @param permissionCode full permission code
     * @param sortOrder      sort
     */
    private void ensureAction(String objectCode, String actionCode, String actionName, String permissionCode,
            int sortOrder) {
        if (actionRepository.findByPermissionCode(permissionCode) != null) {
            return;
        }

        if (actionRepository.findByObjectAndAction(objectCode, actionCode) != null) {
            return;
        }

        actionRepository.insert(JulyPermAction.create(EntityId.generate(), objectCode, actionCode, actionName,
                permissionCode, sortOrder, null, AuditInfo.empty()));
    }
}
