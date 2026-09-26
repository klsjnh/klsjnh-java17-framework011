package com.klsjnh.infrastructure.storagecenter.seed;

/*                StoragePermCatalogSeed011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  storage center permission catalog seed
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
 * Idempotent seed for the storage center permission catalog.
 */

@Component
@ConditionalOnBean(JulyPermObjectRepository.class)
public class StoragePermCatalogSeed011 {

    private static final Logger logger = LoggerFactory.getLogger(StoragePermCatalogSeed011.class);

    private final JulyPermObjectRepository objectRepository;
    private final JulyPermActionRepository actionRepository;

    /**
     * Create the seed.
     *
     * @param objectRepository object catalog
     * @param actionRepository action catalog
     */
    public StoragePermCatalogSeed011(JulyPermObjectRepository objectRepository,
            JulyPermActionRepository actionRepository) {
        this.objectRepository = objectRepository;
        this.actionRepository = actionRepository;
    }

    /**
     * Seed catalog rows when missing.
     */
    public void seed() {
        seedProvider();
        seedBucket();
        seedObject();
        logger.info("storage center perm catalog seed ready");
    }

    /**
     * Seed julyStorageProvider object and actions.
     */
    private void seedProvider() {
        ensureObject("julyStorageProvider", "存储实例", "storagecenter", 30);
        ensureAction("julyStorageProvider", "select", "查看", "storagecenter:julyStorageProvider:select", 1);
        ensureAction("julyStorageProvider", "insert", "新增", "storagecenter:julyStorageProvider:insert", 2);
        ensureAction("julyStorageProvider", "update", "修改", "storagecenter:julyStorageProvider:update", 3);
        ensureAction("julyStorageProvider", "logicDelete", "删除", "storagecenter:julyStorageProvider:logicDelete", 4);
        ensureAction("julyStorageProvider", "testConnection", "测试连接", "storagecenter:julyStorageProvider:testConnection", 5);
    }

    /**
     * Seed julyStorageProviderBucket object and actions.
     */
    private void seedBucket() {
        ensureObject("julyStorageProviderBucket", "存储桶", "storagecenter", 31);
        ensureAction("julyStorageProviderBucket", "select", "查看", "storagecenter:julyStorageProviderBucket:select", 1);
        ensureAction("julyStorageProviderBucket", "insert", "新增", "storagecenter:julyStorageProviderBucket:insert", 2);
        ensureAction("julyStorageProviderBucket", "logicDelete", "删除", "storagecenter:julyStorageProviderBucket:logicDelete", 3);
        ensureAction("julyStorageProviderBucket", "testConnection", "测试连接", "storagecenter:julyStorageProviderBucket:testConnection", 4);
    }

    /**
     * Seed julyStorageObject object and actions.
     */
    private void seedObject() {
        ensureObject("julyStorageObject", "存储对象", "storagecenter", 32);
        ensureAction("julyStorageObject", "select", "查看", "storagecenter:julyStorageObject:select", 1);
        ensureAction("julyStorageObject", "upload", "上传", "storagecenter:julyStorageObject:upload", 2);
        ensureAction("julyStorageObject", "remove", "删除", "storagecenter:julyStorageObject:remove", 3);
        ensureAction("julyStorageObject", "copy", "复制", "storagecenter:julyStorageObject:copy", 4);
        ensureAction("julyStorageObject", "rename", "重命名", "storagecenter:julyStorageObject:rename", 5);
        ensureAction("julyStorageObject", "saveText", "保存文本", "storagecenter:julyStorageObject:saveText", 6);
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
