package com.klsjnh.infrastructure.datasource.seed;

/*                DatasourcePermCatalogSeed011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  datasource center permission catalog seed
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
 * Idempotent seed for the datasource center permission catalog
 * (julyDatasource / julySyncRule / julySql). Active only when
 * {@code center-datasource011-starter} is on the classpath.
 */

@Component
@ConditionalOnBean(JulyPermObjectRepository.class)
public class DatasourcePermCatalogSeed011 {

    private static final Logger logger = LoggerFactory.getLogger(DatasourcePermCatalogSeed011.class);

    private final JulyPermObjectRepository objectRepository;
    private final JulyPermActionRepository actionRepository;

    /**
     * Create the seed.
     *
     * @param objectRepository object catalog
     * @param actionRepository action catalog
     */
    public DatasourcePermCatalogSeed011(JulyPermObjectRepository objectRepository,
            JulyPermActionRepository actionRepository) {
        this.objectRepository = objectRepository;
        this.actionRepository = actionRepository;
    }

    /**
     * Seed catalog rows when missing.
     */
    public void seed() {
        ensureObject("julyDatasource", "数据源", "datasource", 20);
        ensureAction("julyDatasource", "select", "查看", "datasource:julyDatasource:select", 1);
        ensureAction("julyDatasource", "insert", "新增", "datasource:julyDatasource:insert", 2);
        ensureAction("julyDatasource", "update", "修改", "datasource:julyDatasource:update", 3);
        ensureAction("julyDatasource", "logicDelete", "删除", "datasource:julyDatasource:logicDelete", 4);
        ensureAction("julyDatasource", "testConnection", "测试连接", "datasource:julyDatasource:testConnection", 5);
        ensureAction("julyDatasource", "reloadRegistry", "重载注册表", "datasource:julyDatasource:reloadRegistry", 6);

        ensureObject("julySyncRule", "同步规则", "datasource", 21);
        ensureAction("julySyncRule", "select", "查看", "datasource:julySyncRule:select", 1);
        ensureAction("julySyncRule", "insert", "新增", "datasource:julySyncRule:insert", 2);
        ensureAction("julySyncRule", "update", "修改", "datasource:julySyncRule:update", 3);
        ensureAction("julySyncRule", "logicDelete", "删除", "datasource:julySyncRule:logicDelete", 4);
        ensureAction("julySyncRule", "run", "执行", "datasource:julySyncRule:run", 5);

        ensureObject("julySql", "SQL查询", "datasource", 22);
        ensureAction("julySql", "select", "查询", "datasource:julySql:select", 1);

        logger.info("datasource center perm catalog seed ready");
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

        actionRepository.insert(JulyPermAction.create(EntityId.generate(), objectCode, actionCode, actionName,
                permissionCode, sortOrder, null, AuditInfo.empty()));
    }
}