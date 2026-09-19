package com.klsjnh.web.messagecenter.inbound.vo.channel;

/*                JulyInboundChannelVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Channel response VO (detail and page rows). The config is echoed as stored.
 */

@Data
public class JulyInboundChannelVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Channel code, unique, immutable. */
    @Schema(description = "渠道编码（唯一，不可变）")
    private String channelCode;

    /** Manual sort order. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;

    /** Channel display name. */
    @Schema(description = "渠道名称")
    private String channelName;

    /** Provider type (SPI channelCode). */
    @Schema(description = "提供商类型")
    private String providerType;

    /** JSON config. */
    @Schema(description = "渠道配置 JSON")
    private String config;

    /** Row status: 0 disabled / 1 enabled. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Creator. */
    @Schema(description = "创建人")
    private String createBy;

    /** Last modifier. */
    @Schema(description = "最后修改人")
    private String updateBy;

    /** Create time. */
    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    /** Update time. */
    @Schema(description = "最后修改日期")
    private LocalDateTime updateTime;
}
