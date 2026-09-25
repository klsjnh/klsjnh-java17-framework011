package com.klsjnh.web.aicenter.vo.aimodelprovider;

/*                AiModelProviderVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Provider response VO (detail and page rows).
 */

@Data
public class AiModelProviderVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Provider code, unique, immutable. */
    @Schema(description = "提供商编码（唯一，不可变）")
    private String providerCode;

    /** Manual sort order. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;

    /** Provider display name. */
    @Schema(description = "提供商名称")
    private String providerName;

    /** OpenAI-compatible base url. */
    @Schema(description = "接口 Base URL")
    private String baseUrl;

    /** Comma separated model list. */
    @Schema(description = "模型清单（逗号分隔）")
    private String models;

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
