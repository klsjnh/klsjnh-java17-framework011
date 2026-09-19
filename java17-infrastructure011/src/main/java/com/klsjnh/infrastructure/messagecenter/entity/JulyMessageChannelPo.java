package com.klsjnh.infrastructure.messagecenter.entity;

/*                JulyMessageChannelPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Channel persistence PO mapped to july_message_channel, a sorted table
 * (BasePo011 adds sort_order).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_message_channel")
public class JulyMessageChannelPo extends BasePo011 {

    /** Channel code, unique, immutable. */
    private String channelCode;

    /** Channel display name. */
    private String channelName;

    /** Provider type (SPI channelCode). */
    private String providerType;

    /** JSON config (secrets masked on output). */
    private String config;

    /** Remark, optional. */
    private String remark;
}
