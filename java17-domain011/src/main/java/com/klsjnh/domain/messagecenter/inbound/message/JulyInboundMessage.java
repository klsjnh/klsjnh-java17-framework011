package com.klsjnh.domain.messagecenter.inbound.message;

/*                JulyInboundMessage class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july inbound message class
 *
 */

import com.klsjnh.common.enums.MessageInboundStatus011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyInboundMessage aggregate root (message center context): one received
 * record — the channel used, the sender, the parsed content, the channel-side
 * message id and the handling outcome.
 */

public class JulyInboundMessage {

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Channel code.
     */
    private String channelCode;

    /**
     * Provider type that parsed the message (the SPI channelCode).
     */
    private String providerType;

    /**
     * Channel-defined message shape (open string), optional.
     */
    private String messageType;

    /**
     * Channel-defined message payload as JSON, optional.
     */
    private String payload;

    /**
     * Sender, optional.
     */
    private String fromId;

    /**
     * Parsed content, optional.
     */
    private String content;

    /**
     * Channel-side message id (dedupe key), optional.
     */
    private String rawMessageId;

    /**
     * Handling failure reason, optional.
     */
    private String error;

    /**
     * Remark, optional.
     */
    private String remark;

    /**
     * Handling status (business status of this record): a
     * {@link MessageInboundStatus011} numeric code ("1" received / "2" handled /
     * "3" failed).
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
     * @param fromId       sender, optional
     * @param content      parsed content, optional
     * @param rawMessageId channel-side message id, optional
     * @param error        handling failure reason, optional
     * @param remark       remark, optional
     * @param status       handling status (MessageInboundStatus011 code)
     * @param audit        audit info
     */
    public JulyInboundMessage(EntityId id, String channelCode, String providerType, String messageType, String payload,
            String fromId, String content, String rawMessageId, String error, String remark, String status,
            AuditInfo audit) {
        validate(channelCode, providerType, fromId, rawMessageId, remark);

        this.id = id;
        this.channelCode = channelCode;
        this.providerType = providerType;
        this.messageType = messageType;
        this.payload = payload;
        this.fromId = fromId;
        this.content = content;
        this.rawMessageId = rawMessageId;
        this.error = error;
        this.status = StringUtil011.isBlank(status) ? MessageInboundStatus011.RECEIVED.getCode() : status;
        this.remark = remark;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new received record.
     *
     * @param id           primary key
     * @param channelCode  channel code, max 60
     * @param providerType provider type, max 60
     * @param messageType  channel-defined message shape, optional, max 60
     * @param payload      channel-defined payload JSON, optional
     * @param fromId       sender, optional, max 300
     * @param content      parsed content, optional
     * @param rawMessageId channel-side message id, optional, max 120
     * @param remark       remark, optional, max 300
     * @param audit        audit info
     * @return new aggregate
     */
    public static JulyInboundMessage create(EntityId id, String channelCode, String providerType, String messageType,
            String payload, String fromId, String content, String rawMessageId, String remark, AuditInfo audit) {
        return new JulyInboundMessage(id, channelCode, providerType, messageType, payload, fromId, content,
                rawMessageId, null, remark, MessageInboundStatus011.RECEIVED.getCode(), audit);
    }

    /**
     * Mark this record handled (status HANDLED, error cleared).
     */
    public void markHandled() {
        this.status = MessageInboundStatus011.HANDLED.getCode();
        this.error = null;
    }

    /**
     * Mark this record failed (status FAILED).
     *
     * @param error failure reason
     */
    public void markFailed(String error) {
        this.status = MessageInboundStatus011.FAILED.getCode();
        this.error = error;
    }

    /**
     * Validate the message fields.
     *
     * @param channelCode  channel code
     * @param providerType provider type
     * @param fromId       sender
     * @param rawMessageId channel-side message id
     * @param remark       remark
     */
    private static void validate(String channelCode, String providerType, String fromId, String rawMessageId,
            String remark) {
        StringUtil011.requirePresent(channelCode, "channel code", 60);
        StringUtil011.requirePresent(providerType, "provider type", 60);
        StringUtil011.requireMax(fromId, "from", 300);
        StringUtil011.requireMax(rawMessageId, "raw message id", 120);
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
     * Get the sender.
     *
     * @return sender or null
     */
    public String fromId() {
        return fromId;
    }

    /**
     * Get the parsed content.
     *
     * @return content or null
     */
    public String content() {
        return content;
    }

    /**
     * Get the channel-side message id.
     *
     * @return raw message id or null
     */
    public String rawMessageId() {
        return rawMessageId;
    }

    /**
     * Get the handling failure reason.
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
     * Get the handling status (MessageInboundStatus011 code).
     *
     * @return "1" received / "2" handled / "3" failed
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
