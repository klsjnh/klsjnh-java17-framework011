package com.klsjnh.domain.messagecenter.message;

/*                JulyMessage class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.messagecenter.channel.MessageResult;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyMessage aggregate root (message center context): one send record / outbox
 * row — the channel used, the receiver, the rendered content and the outcome.
 */

public class JulyMessage {

    /**
     * Send status: pending.
     */
    public static final String STATUS_PENDING = "PENDING";

    /**
     * Send status: success.
     */
    public static final String STATUS_SUCCESS = "SUCCESS";

    /**
     * Send status: failed.
     */
    public static final String STATUS_FAILED = "FAILED";

    /**
     * Default sort order when none is given (matches BasePo011 / DDL default).
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Manual sort order, smaller comes first.
     */
    private Integer sortOrder;

    /**
     * Channel code.
     */
    private String channelCode;

    /**
     * Provider type actually used (the SPI channelCode).
     */
    private String providerType;

    /**
     * Channel-defined message shape (open string), optional.
     */
    private String messageType;

    /**
     * Channel-defined message payload as JSON (shape-specific keys such as
     * image_key / file_key / card json), optional.
     */
    private String payload;

    /**
     * Receiver, optional.
     */
    private String msgTo;

    /**
     * Template code, optional.
     */
    private String templateCode;

    /**
     * Rendered title, optional.
     */
    private String title;

    /**
     * Rendered content, optional.
     */
    private String content;

    /**
     * Send status: PENDING / SUCCESS / FAILED.
     */
    private String sendStatus;

    /**
     * Retry count.
     */
    private int retryCount;

    /**
     * Failure reason, optional.
     */
    private String error;

    /**
     * Remark, optional.
     */
    private String remark;

    /**
     * Row status: '1' enabled / '0' disabled.
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id           primary key
     * @param sortOrder    manual sort order, null falls back to the default
     * @param channelCode  channel code
     * @param providerType provider type
     * @param messageType  channel-defined message shape, optional
     * @param payload      channel-defined payload JSON, optional
     * @param msgTo        receiver, optional
     * @param templateCode template code, optional
     * @param title        title, optional
     * @param content      content, optional
     * @param sendStatus   send status
     * @param retryCount   retry count
     * @param error        failure reason, optional
     * @param status       row status
     * @param remark       remark, optional
     * @param audit        audit info
     */
    public JulyMessage(EntityId id, Integer sortOrder, String channelCode, String providerType, String messageType,
            String payload, String msgTo, String templateCode, String title, String content, String sendStatus,
            int retryCount, String error, String status, String remark, AuditInfo audit) {
        validate(channelCode, providerType, msgTo, remark);

        this.id = id;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.channelCode = channelCode;
        this.providerType = providerType;
        this.messageType = messageType;
        this.payload = payload;
        this.msgTo = msgTo;
        this.templateCode = templateCode;
        this.title = title;
        this.content = content;
        this.sendStatus = StringUtil011.isBlank(sendStatus) ? STATUS_PENDING : sendStatus;
        this.retryCount = retryCount;
        this.error = error;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new send record.
     *
     * @param id           primary key
     * @param sortOrder    manual sort order, null falls back to the default
     * @param channelCode  channel code, max 60
     * @param providerType provider type, max 60
     * @param messageType  channel-defined message shape, optional, max 60
     * @param payload      channel-defined payload JSON, optional
     * @param msgTo        receiver, optional, max 300
     * @param templateCode template code, optional
     * @param title        title, optional, max 300
     * @param content      content, optional
     * @param remark       remark, optional, max 300
     * @param audit        audit info
     * @return new aggregate
     */
    public static JulyMessage create(EntityId id, Integer sortOrder, String channelCode, String providerType,
            String messageType, String payload, String msgTo, String templateCode, String title, String content,
            String remark, AuditInfo audit) {
        return new JulyMessage(id, sortOrder, channelCode, providerType, messageType, payload, msgTo, templateCode,
                title, content, STATUS_PENDING, 0, null, Status011.ENABLED.getCode(), remark, audit);
    }

    /**
     * Apply the channel outcome to this record.
     *
     * @param result channel result
     */
    public void markResult(MessageResult result) {
        if (result != null && result.success()) {
            this.sendStatus = STATUS_SUCCESS;
            this.error = null;
        } else {
            this.sendStatus = STATUS_FAILED;
            this.error = result == null ? "unknown error" : result.error();
        }
    }

    /**
     * Bump the retry counter (called before a resend attempt).
     */
    public void countRetry() {
        this.retryCount = this.retryCount + 1;
    }

    /**
     * Validate the message fields.
     *
     * @param channelCode  channel code
     * @param providerType provider type
     * @param msgTo        receiver
     * @param remark       remark
     */
    private static void validate(String channelCode, String providerType, String msgTo, String remark) {
        StringUtil011.requirePresent(channelCode, "channel code", 60);
        StringUtil011.requirePresent(providerType, "provider type", 60);
        StringUtil011.requireMax(msgTo, "to", 300);
        StringUtil011.requireMax(remark, "remark", 300);
    }

    /**
     * Get the primary key.
     *
     * @return id value object
     */
    public EntityId id() {
        return id;
    }

    /**
     * Get the manual sort order.
     *
     * @return sort order
     */
    public Integer sortOrder() {
        return sortOrder;
    }

    /**
     * Get the channel code.
     *
     * @return channel code
     */
    public String channelCode() {
        return channelCode;
    }

    /**
     * Get the provider type.
     *
     * @return provider type
     */
    public String providerType() {
        return providerType;
    }

    /**
     * Get the channel-defined message shape.
     *
     * @return message type or null
     */
    public String messageType() {
        return messageType;
    }

    /**
     * Get the channel-defined payload JSON.
     *
     * @return payload or null
     */
    public String payload() {
        return payload;
    }

    /**
     * Get the receiver.
     *
     * @return receiver or null
     */
    public String msgTo() {
        return msgTo;
    }

    /**
     * Get the template code.
     *
     * @return template code or null
     */
    public String templateCode() {
        return templateCode;
    }

    /**
     * Get the title.
     *
     * @return title or null
     */
    public String title() {
        return title;
    }

    /**
     * Get the content.
     *
     * @return content or null
     */
    public String content() {
        return content;
    }

    /**
     * Get the send status.
     *
     * @return PENDING / SUCCESS / FAILED
     */
    public String sendStatus() {
        return sendStatus;
    }

    /**
     * Get the retry count.
     *
     * @return retry count
     */
    public int retryCount() {
        return retryCount;
    }

    /**
     * Get the failure reason.
     *
     * @return error or null
     */
    public String error() {
        return error;
    }

    /**
     * Get the remark.
     *
     * @return remark or null
     */
    public String remark() {
        return remark;
    }

    /**
     * Get the row status.
     *
     * @return '1' enabled / '0' disabled
     */
    public String status() {
        return status;
    }

    /**
     * Get the audit info.
     *
     * @return audit info
     */
    public AuditInfo audit() {
        return audit;
    }
}
