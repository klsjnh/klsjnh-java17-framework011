package com.klsjnh.web.lowcode011.vo;

/*                JulyMetadataDisplayVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata display vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Display column transport VO.
 */

@Data
public class JulyMetadataDisplayVo011 {

    /** Column code binding a field. */
    @Schema(description = "列编码（绑定字段）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String displayCode;

    /** Column name. */
    @Schema(description = "列名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String displayName;

    /** Text alignment. */
    @Schema(description = "对齐（left/center/right）")
    private String align;

    /** Column width. */
    @Schema(description = "列宽（px）")
    private Integer width;

    /** Component type. */
    @Schema(description = "组件类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String componentType;

    /** Display type code. */
    @Schema(description = "显示类型（DisplayType011）")
    private String displayType;

    /** Extra parameter. */
    @Schema(description = "扩展参数")
    private String param011;

    /** Sort order. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;
}
