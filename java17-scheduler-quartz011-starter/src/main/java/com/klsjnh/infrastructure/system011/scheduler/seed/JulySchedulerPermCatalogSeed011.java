package com.klsjnh.infrastructure.system011.scheduler.seed;

/*                JulySchedulerPermCatalogSeed011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  julyScheduler permission catalog seed (moved from core)
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * Idempotent seed for the julyScheduler permission catalog (including start /
 * stop / executeOnce). Registered only when the quartz scheduler starter is on
 * the classpath (codes must match {@code JulySchedulerPermissionCodes011}).
 */

@Component
@ConditionalOnBean(JulyPermObjectRepository.class)
public class JulySchedulerPermCatalogSeed011 {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(JulySchedulerPermCatalogSeed011.class);

    /** Must match JulySchedulerPermissionCodes011.SELECT. */
    private static final String SELECT = "system011:julyScheduler:select";
    /** Must match JulySchedulerPermissionCodes011.INSERT. */
    private static final String INSERT = "system011:julyScheduler:insert";
    /** Must match JulySchedulerPermissionCodes011.UPDATE. */
    private static final String UPDATE = "system011:julyScheduler:update";
    /** Must match JulySchedulerPermissionCodes011.LOGIC_DELETE. */
    private static final String LOGIC_DELETE = "system011:julyScheduler:logicDelete";
    /** Must match JulySchedulerPermissionCodes011.START. */
    private static final String START = "system011:julyScheduler:start";
    /** Must match JulySchedulerPermissionCodes011.STOP. */
    private static final String STOP = "system011:julyScheduler:stop";
    /** Must match JulySchedulerPermissionCodes011.EXECUTE_ONCE. */
    private static final String EXECUTE_ONCE = "system011:julyScheduler:executeOnce";

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
    public JulySchedulerPermCatalogSeed011(JulyPermObjectRepository objectRepository,
            JulyPermActionRepository actionRepository) {
        this.objectRepository = objectRepository;
        this.actionRepository = actionRepository;
    }

    /**
     * Seed julyScheduler catalog rows when missing.
     */
    public void seed() {
        String funcName = "july scheduler perm catalog seed";

        ensureObject("julyScheduler", "定时任务", "system011", 20);
        ensureAction("julyScheduler", "select", "查看", SELECT, 1);
        ensureAction("julyScheduler", "insert", "新增", INSERT, 2);
        ensureAction("julyScheduler", "update", "修改", UPDATE, 3);
        ensureAction("julyScheduler", "logicDelete", "删除", LOGIC_DELETE, 4);
        ensureAction("julyScheduler", "start", "启动", START, 5);
        ensureAction("julyScheduler", "stop", "停止", STOP, 6);
        ensureAction("julyScheduler", "executeOnce", "执行一次", EXECUTE_ONCE, 7);

        logger.info("{} ready", funcName);
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
