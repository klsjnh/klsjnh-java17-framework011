package com.klsjnh.infrastructure.iam.perm.seed;

/*                JulyPermCatalogSeed011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  seed julyConfig / julyScheduler permission catalog
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.domain.iam.perm.JulyPermAction;
import com.klsjnh.domain.iam.perm.JulyPermActionRepository;
import com.klsjnh.domain.iam.perm.JulyPermObject;
import com.klsjnh.domain.iam.perm.JulyPermObjectRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.config.JulyConfigPermissionCodes011;
import com.klsjnh.domain.system011.scheduler.JulySchedulerPermissionCodes011;

import org.springframework.stereotype.Component;

/**
 * Idempotent seed for the P3 permission catalog blueprints: julyConfig and
 * julyScheduler objects plus their actions (permission codes must match the
 * UseCase assertHas constants).
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
     * Seed julyConfig + julyScheduler catalog rows when missing.
     */
    public void seed() {
        String funcName = "perm catalog seed";

        ensureObject("julyConfig", "系统配置", "system011", 10);
        ensureAction("julyConfig", "select", "查看", JulyConfigPermissionCodes011.SELECT, 1);
        ensureAction("julyConfig", "insert", "新增", JulyConfigPermissionCodes011.INSERT, 2);
        ensureAction("julyConfig", "update", "修改", JulyConfigPermissionCodes011.UPDATE, 3);
        ensureAction("julyConfig", "logicDelete", "删除", JulyConfigPermissionCodes011.LOGIC_DELETE, 4);
        ensureAction("julyConfig", "export", "导出", JulyConfigPermissionCodes011.EXPORT, 5);
        ensureAction("julyConfig", "backup", "备份", JulyConfigPermissionCodes011.BACKUP, 6);

        ensureObject("julyScheduler", "定时任务", "system011", 20);
        ensureAction("julyScheduler", "select", "查看", JulySchedulerPermissionCodes011.SELECT, 1);
        ensureAction("julyScheduler", "insert", "新增", JulySchedulerPermissionCodes011.INSERT, 2);
        ensureAction("julyScheduler", "update", "修改", JulySchedulerPermissionCodes011.UPDATE, 3);
        ensureAction("julyScheduler", "logicDelete", "删除", JulySchedulerPermissionCodes011.LOGIC_DELETE, 4);
        ensureAction("julyScheduler", "start", "启动", JulySchedulerPermissionCodes011.START, 5);
        ensureAction("julyScheduler", "stop", "停止", JulySchedulerPermissionCodes011.STOP, 6);
        ensureAction("julyScheduler", "executeOnce", "执行一次", JulySchedulerPermissionCodes011.EXECUTE_ONCE, 7);

        logger.info("{} ready (julyConfig + julyScheduler)", funcName);
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
