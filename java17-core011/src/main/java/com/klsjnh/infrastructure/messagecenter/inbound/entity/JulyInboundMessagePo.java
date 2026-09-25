package com.klsjnh.infrastructure.messagecenter.inbound.entity;

/*                JulyInboundMessagePo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july inbound message po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Inbound record persistence PO mapped to july_message_inbound (no sort_order:
 * a received record is ordered by its create time, not manually). No business
 * unique key; the inherited status column carries the inbound handling status.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_message_inbound")
public class JulyInboundMessagePo extends BasePo {

    /** Channel code. */
    private String channelCode;

    /** Provider type that parsed the message. */
    private String providerType;

    /** Channel-defined message shape, optional. */
    private String messageType;

    /** Channel-defined payload JSON, optional. */
    private String payload;

    /** Sender, optional. */
    private String fromId;

    /** Parsed content, optional. */
    private String content;

    /** Channel-side message id, optional. */
    private String rawMessageId;

    /** Handling failure reason, optional. */
    private String error;

    /** Remark, optional. */
    private String remark;
}
