package com.klsjnh.infrastructure.messagecenter.outbound.entity;

/*                JulyOutboundMessagePo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Send record persistence PO mapped to july_message_outbound (no sort_order: a send
 * record is ordered by its create time, not manually). No business unique key.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_message_outbound")
public class JulyOutboundMessagePo extends BasePo {

    /** Channel code. */
    private String channelCode;

    /** Provider type actually used. */
    private String providerType;

    /** Channel-defined message shape, optional. */
    private String messageType;

    /** Channel-defined payload JSON, optional. */
    private String payload;

    /** Receiver, optional. */
    private String msgTo;

    /** Template code, optional. */
    private String templateCode;

    /** Rendered title, optional. */
    private String title;

    /** Rendered content, optional. */
    private String content;

    /** Retry count. */
    private Integer retryCount;

    /** Failure reason, optional. */
    private String error;

    /** Remark, optional. */
    private String remark;
}
