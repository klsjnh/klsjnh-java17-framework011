package com.klsjnh.web.messagecenter.vo.channel;

/*                JulyOutboundChannelInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Insert request VO for a message channel (channelCode required and immutable).
 */

@Data
public class JulyOutboundChannelInsertVo011 {

    /** Channel code, unique, immutable, max 60. */
    @Schema(description = "渠道编码（唯一，最长 60，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channelCode;

    /** Manual sort order, smaller comes first; blank falls back to the default. */
    @Schema(description = "排序（越小越靠前，留空取默认 9999）")
    private Integer sortOrder;

    /** Channel display name, max 100. */
    @Schema(description = "渠道名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channelName;

    /** Provider type (SPI channelCode), max 60. */
    @Schema(description = "提供商类型（绑定 SPI channelCode，如 inapp / webhook / sms，最长 60）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String providerType;

    /** JSON config (url / secret ...). */
    @Schema(description = "渠道配置 JSON（如 {\"url\":\"...\"}）")
    private String config;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}
