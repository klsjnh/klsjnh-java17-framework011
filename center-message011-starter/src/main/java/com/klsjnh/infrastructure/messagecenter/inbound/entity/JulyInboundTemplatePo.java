package com.klsjnh.infrastructure.messagecenter.inbound.entity;

/*                JulyInboundTemplatePo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Template persistence PO mapped to july_message_inbound_template, a sorted table
 * (BasePo011 adds sort_order).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_message_inbound_template")
public class JulyInboundTemplatePo extends BasePo011 {

    /** Template code, unique, immutable. */
    private String templateCode;

    /** Template display name. */
    private String templateName;

    /** Channel code the template is bound to. */
    private String channelCode;

    /** Title with ${var} placeholders. */
    private String title;

    /** Content body with ${var} placeholders. */
    private String content;

    /** Remark, optional. */
    private String remark;
}
