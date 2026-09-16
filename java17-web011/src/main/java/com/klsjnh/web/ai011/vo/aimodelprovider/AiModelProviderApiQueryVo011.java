package com.klsjnh.web.ai011.vo.aimodelprovider;

/*                AiModelProviderApiQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider api query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Api key list request VO: keys of one provider, optional status filter.
 */

@Data
public class AiModelProviderApiQueryVo011 {

    /** Provider code. */
    @Schema(description = "提供商编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String providerCode;

    /** Row status filter, blank for all. */
    @Schema(description = "状态过滤（0 停用 / 1 启用，留空为全部）")
    private String status;
}
