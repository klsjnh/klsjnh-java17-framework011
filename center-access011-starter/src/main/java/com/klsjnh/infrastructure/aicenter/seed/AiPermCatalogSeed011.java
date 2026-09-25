package com.klsjnh.infrastructure.aicenter.seed;

/*                AiPermCatalogSeed011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  ai center permission catalog seed
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * Idempotent seed for the AI center permission catalog. Registered only when
 * the ai center starter is on the classpath (codes must match the
 * {@code *PermissionCodes011} classes in center-ai011-starter).
 */

@Component
@ConditionalOnClass(name = "com.klsjnh.domain.aicenter.modelprovider.AiModelProviderPermissionCodes011")
public class AiPermCatalogSeed011 {

    private static final Logger logger = LoggerFactory.getLogger(AiPermCatalogSeed011.class);

    private final JulyPermObjectRepository objectRepository;
    private final JulyPermActionRepository actionRepository;

    /**
     * Create the seed.
     *
     * @param objectRepository object catalog
     * @param actionRepository action catalog
     */
    public AiPermCatalogSeed011(JulyPermObjectRepository objectRepository,
            JulyPermActionRepository actionRepository) {
        this.objectRepository = objectRepository;
        this.actionRepository = actionRepository;
    }

    /**
     * Seed catalog rows when missing.
     */
    public void seed() {
        seedModelProvider();
        seedAiDomain();
        seedInference();
        seedImage();
        seedAudio();
        logger.info("ai center perm catalog seed ready");
    }

    /**
     * Seed julyAiModelProvider object and actions.
     */
    private void seedModelProvider() {
        ensureObject("julyAiModelProvider", "AI模型提供商", "aicenter", 10);
        ensureAction("julyAiModelProvider", "select", "查看", "aicenter:julyAiModelProvider:select", 1);
        ensureAction("julyAiModelProvider", "insert", "新增", "aicenter:julyAiModelProvider:insert", 2);
        ensureAction("julyAiModelProvider", "update", "修改", "aicenter:julyAiModelProvider:update", 3);
        ensureAction("julyAiModelProvider", "logicDelete", "删除", "aicenter:julyAiModelProvider:logicDelete", 4);
        ensureAction("julyAiModelProvider", "testConnection", "测试连接", "aicenter:julyAiModelProvider:testConnection", 5);
        ensureAction("julyAiModelProvider", "export", "导出", "aicenter:julyAiModelProvider:export", 6);
    }

    /**
     * Seed julyAiDomain object and actions.
     */
    private void seedAiDomain() {
        ensureObject("julyAiDomain", "AI业务域", "aicenter", 11);
        ensureAction("julyAiDomain", "select", "查看", "aicenter:julyAiDomain:select", 1);
        ensureAction("julyAiDomain", "insert", "新增", "aicenter:julyAiDomain:insert", 2);
        ensureAction("julyAiDomain", "update", "修改", "aicenter:julyAiDomain:update", 3);
        ensureAction("julyAiDomain", "logicDelete", "删除", "aicenter:julyAiDomain:logicDelete", 4);
        ensureAction("julyAiDomain", "render", "渲染", "aicenter:julyAiDomain:render", 5);
    }

    /**
     * Seed aiInference object and actions.
     */
    private void seedInference() {
        ensureObject("aiInference", "AI推理", "aicenter", 12);
        ensureAction("aiInference", "chat", "对话", "aicenter:aiInference:chat", 1);
    }

    /**
     * Seed aiImage object and actions.
     */
    private void seedImage() {
        ensureObject("aiImage", "AI文生图", "aicenter", 13);
        ensureAction("aiImage", "generate", "生成", "aicenter:aiImage:generate", 1);
    }

    /**
     * Seed aiAudio object and actions.
     */
    private void seedAudio() {
        ensureObject("aiAudio", "AI语音", "aicenter", 14);
        ensureAction("aiAudio", "synthesize", "合成", "aicenter:aiAudio:synthesize", 1);
        ensureAction("aiAudio", "recognize", "识别", "aicenter:aiAudio:recognize", 2);
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
