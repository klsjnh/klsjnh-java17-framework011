package com.klsjnh.web.ai011.vo.aimodelprovider;

/*                AiModelProviderApiVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider api vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Api key response VO.
 * <p>
 * There is deliberately NO {@code apiKey} field: the key is write-only, and
 * keeping it out of the type is the strongest guarantee it can never be echoed
 * back.
 * </p>
 */

@Data
public class AiModelProviderApiVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Api key code, unique within the provider. */
    @Schema(description = "密钥编码（同提供商内唯一）")
    private String apiCode;

    /** Api key display name. */
    @Schema(description = "密钥名称")
    private String apiName;

    /** Manual sort order. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;

    /** Row status: 0 disabled / 1 enabled. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;
}
