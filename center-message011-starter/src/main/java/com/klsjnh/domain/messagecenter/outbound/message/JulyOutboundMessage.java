package com.klsjnh.domain.messagecenter.outbound.message;

/*                JulyOutboundMessage class
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

import com.klsjnh.common.enums.MessageStatus011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.messagecenter.outbound.channel.MessageResult;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyOutboundMessage aggregate root (message center context): one send record / outbox
 * row — the channel used, the receiver, the rendered content and the outcome.
 */

public class JulyOutboundMessage {

    /**
     * Primary key.
     */
    private final EntityId id;

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
     * Send status (business status of this record): a {@link MessageStatus011}
     * numeric code ("1" pending / "2" success / "3" failed).
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
     * @param channelCode  channel code
     * @param providerType provider type
     * @param messageType  channel-defined message shape, optional
     * @param payload      channel-defined payload JSON, optional
     * @param msgTo        receiver, optional
     * @param templateCode template code, optional
     * @param title        title, optional
     * @param content      content, optional
     * @param status       send status (MessageStatus011 code)
     * @param retryCount   retry count
     * @param error        failure reason, optional
     * @param remark       remark, optional
     * @param audit        audit info
     */
    public JulyOutboundMessage(EntityId id, String channelCode, String providerType, String messageType, String payload,
            String msgTo, String templateCode, String title, String content, String status, int retryCount,
            String error, String remark, AuditInfo audit) {
        validate(channelCode, providerType, msgTo, remark);

        this.id = id;
        this.channelCode = channelCode;
        this.providerType = providerType;
        this.messageType = messageType;
        this.payload = payload;
        this.msgTo = msgTo;
        this.templateCode = templateCode;
        this.title = title;
        this.content = content;
        this.status = StringUtil011.isBlank(status) ? MessageStatus011.PENDING.getCode() : status;
        this.retryCount = retryCount;
        this.error = error;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new send record.
     *
     * @param id           primary key
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
    public static JulyOutboundMessage create(EntityId id, String channelCode, String providerType, String messageType,
            String payload, String msgTo, String templateCode, String title, String content, String remark,
            AuditInfo audit) {
        return new JulyOutboundMessage(id, channelCode, providerType, messageType, payload, msgTo, templateCode, title, content,
                MessageStatus011.PENDING.getCode(), 0, null, remark, audit);
    }

    /**
     * Apply the channel outcome to this record (status + error).
     *
     * @param result channel result
     */
    public void markResult(MessageResult result) {
        if (result != null && result.success()) {
            this.status = MessageStatus011.SUCCESS.getCode();
            this.error = null;
        } else {
            this.status = MessageStatus011.FAILED.getCode();
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
     * Get the send status (MessageStatus011 code).
     *
     * @return "1" pending / "2" success / "3" failed
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
