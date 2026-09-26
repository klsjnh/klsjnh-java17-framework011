package com.klsjnh.infrastructure.system011.platform.seed;

/*                JulyPlatformPermCatalogSeed011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  julyConfig / julyDictionary permission catalog seed
 *                  (activated when center-platform011-starter is present)
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
 * Idempotent seed for the platform center permission catalog (julyConfig /
 * julyDictionary). Registered only when {@code center-platform011-starter} is
 * on the classpath (codes must match the platform UseCase assertHas constants).
 */

@Component
@ConditionalOnBean(JulyPermObjectRepository.class)
public class JulyPlatformPermCatalogSeed011 {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(JulyPlatformPermCatalogSeed011.class);

    /** Must match JulyConfigPermissionCodes011.SELECT. */
    private static final String CONFIG_SELECT = "system011:julyConfig:select";
    /** Must match JulyConfigPermissionCodes011.INSERT. */
    private static final String CONFIG_INSERT = "system011:julyConfig:insert";
    /** Must match JulyConfigPermissionCodes011.UPDATE. */
    private static final String CONFIG_UPDATE = "system011:julyConfig:update";
    /** Must match JulyConfigPermissionCodes011.LOGIC_DELETE. */
    private static final String CONFIG_LOGIC_DELETE = "system011:julyConfig:logicDelete";
    /** Must match JulyConfigPermissionCodes011.EXPORT. */
    private static final String CONFIG_EXPORT = "system011:julyConfig:export";
    /** Must match JulyConfigPermissionCodes011.BACKUP. */
    private static final String CONFIG_BACKUP = "system011:julyConfig:backup";

    /** Must match JulyDictionaryPermissionCodes011.SELECT. */
    private static final String DICT_SELECT = "system011:julyDictionary:select";
    /** Must match JulyDictionaryPermissionCodes011.INSERT. */
    private static final String DICT_INSERT = "system011:julyDictionary:insert";
    /** Must match JulyDictionaryPermissionCodes011.UPDATE. */
    private static final String DICT_UPDATE = "system011:julyDictionary:update";
    /** Must match JulyDictionaryPermissionCodes011.LOGIC_DELETE. */
    private static final String DICT_LOGIC_DELETE = "system011:julyDictionary:logicDelete";
    /** Must match JulyDictionaryPermissionCodes011.EXPORT. */
    private static final String DICT_EXPORT = "system011:julyDictionary:export";
    /** Must match JulyDictionaryPermissionCodes011.IMPORT. */
    private static final String DICT_IMPORT = "system011:julyDictionary:import";

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
    public JulyPlatformPermCatalogSeed011(JulyPermObjectRepository objectRepository,
            JulyPermActionRepository actionRepository) {
        this.objectRepository = objectRepository;
        this.actionRepository = actionRepository;
    }

    /**
     * Seed catalog rows when missing.
     */
    public void seed() {
        String funcName = "platform perm catalog seed";

        ensureObject("julyConfig", "系统配置", "system011", 10);
        ensureAction("julyConfig", "select", "查看", CONFIG_SELECT, 1);
        ensureAction("julyConfig", "insert", "新增", CONFIG_INSERT, 2);
        ensureAction("julyConfig", "update", "修改", CONFIG_UPDATE, 3);
        ensureAction("julyConfig", "logicDelete", "删除", CONFIG_LOGIC_DELETE, 4);
        ensureAction("julyConfig", "export", "导出", CONFIG_EXPORT, 5);
        ensureAction("julyConfig", "backup", "备份", CONFIG_BACKUP, 6);

        ensureObject("julyDictionary", "数据字典", "system011", 11);
        ensureAction("julyDictionary", "select", "查看", DICT_SELECT, 1);
        ensureAction("julyDictionary", "insert", "新增", DICT_INSERT, 2);
        ensureAction("julyDictionary", "update", "修改", DICT_UPDATE, 3);
        ensureAction("julyDictionary", "logicDelete", "删除", DICT_LOGIC_DELETE, 4);
        ensureAction("julyDictionary", "export", "导出", DICT_EXPORT, 5);
        ensureAction("julyDictionary", "import", "导入", DICT_IMPORT, 6);

        logger.info("{} ready (config + dictionary)", funcName);
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
