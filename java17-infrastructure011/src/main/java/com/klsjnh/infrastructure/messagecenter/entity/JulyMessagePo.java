package com.klsjnh.infrastructure.messagecenter.entity;

/*                JulyMessagePo class
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

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Send record persistence PO mapped to july_message, a sorted table
 * (BasePo011 adds sort_order). No business unique key.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_message")
public class JulyMessagePo extends BasePo011 {

    /** Channel code. */
    private String channelCode;

    /** Provider type actually used. */
    private String providerType;

    /** Receiver, optional. */
    private String msgTo;

    /** Template code, optional. */
    private String templateCode;

    /** Rendered title, optional. */
    private String title;

    /** Rendered content, optional. */
    private String content;

    /** Send status: PENDING / SUCCESS / FAILED. */
    private String sendStatus;

    /** Retry count. */
    private Integer retryCount;

    /** Failure reason, optional. */
    private String error;

    /** Remark, optional. */
    private String remark;
}
