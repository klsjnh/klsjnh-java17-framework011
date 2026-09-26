package com.klsjnh.infrastructure.messagecenter.seed;

/*                MessagePermCatalogSeed011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  message center permission catalog seed
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
 * Idempotent seed for the message center permission catalog.
 */

@Component
@ConditionalOnBean(JulyPermObjectRepository.class)
public class MessagePermCatalogSeed011 {

    private static final Logger logger = LoggerFactory.getLogger(MessagePermCatalogSeed011.class);

    private final JulyPermObjectRepository objectRepository;
    private final JulyPermActionRepository actionRepository;

    /**
     * Create the seed.
     *
     * @param objectRepository object catalog
     * @param actionRepository action catalog
     */
    public MessagePermCatalogSeed011(JulyPermObjectRepository objectRepository,
            JulyPermActionRepository actionRepository) {
        this.objectRepository = objectRepository;
        this.actionRepository = actionRepository;
    }

    /**
     * Seed catalog rows when missing.
     */
    public void seed() {
        seedCrud("julyMessageOutboundChannel", "出站渠道", "messagecenter", 20,
                "messagecenter:julyMessageOutboundChannel");
        seedCrud("julyMessageOutboundTemplate", "出站模板", "messagecenter", 21,
                "messagecenter:julyMessageOutboundTemplate");
        seedOutboundMessage();
        seedCrud("julyMessageInboundChannel", "入站渠道", "messagecenter", 23,
                "messagecenter:julyMessageInboundChannel");
        seedCrud("julyMessageInboundTemplate", "入站模板", "messagecenter", 24,
                "messagecenter:julyMessageInboundTemplate");
        seedInboundMessage();
        logger.info("message center perm catalog seed ready");
    }

    /**
     * Seed a standard CRUD object and its select/insert/update/logicDelete actions.
     *
     * @param objectCode object code
     * @param objectName display name
     * @param moduleCode module
     * @param sortOrder  object sort
     * @param prefix     permission-code prefix
     */
    private void seedCrud(String objectCode, String objectName, String moduleCode, int sortOrder, String prefix) {
        ensureObject(objectCode, objectName, moduleCode, sortOrder);
        ensureAction(objectCode, "select", "查看", prefix + ":select", 1);
        ensureAction(objectCode, "insert", "新增", prefix + ":insert", 2);
        ensureAction(objectCode, "update", "修改", prefix + ":update", 3);
        ensureAction(objectCode, "logicDelete", "删除", prefix + ":logicDelete", 4);
    }

    /**
     * Seed julyMessageOutbound object and actions.
     */
    private void seedOutboundMessage() {
        ensureObject("julyMessageOutbound", "出站消息", "messagecenter", 22);
        ensureAction("julyMessageOutbound", "select", "查看", "messagecenter:julyMessageOutbound:select", 1);
        ensureAction("julyMessageOutbound", "send", "发送", "messagecenter:julyMessageOutbound:send", 2);
        ensureAction("julyMessageOutbound", "resend", "重发", "messagecenter:julyMessageOutbound:resend", 3);
        ensureAction("julyMessageOutbound", "logicDelete", "删除", "messagecenter:julyMessageOutbound:logicDelete", 4);
    }

    /**
     * Seed julyMessageInbound object and actions.
     */
    private void seedInboundMessage() {
        ensureObject("julyMessageInbound", "入站消息", "messagecenter", 25);
        ensureAction("julyMessageInbound", "select", "查看", "messagecenter:julyMessageInbound:select", 1);
        ensureAction("julyMessageInbound", "receive", "接收", "messagecenter:julyMessageInbound:receive", 2);
        ensureAction("julyMessageInbound", "logicDelete", "删除", "messagecenter:julyMessageInbound:logicDelete", 3);
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
