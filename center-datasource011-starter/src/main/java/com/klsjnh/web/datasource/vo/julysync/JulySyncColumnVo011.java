package com.klsjnh.web.datasource.vo.julysync;

/*                JulySyncColumnVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync column vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * One column mapping row (source column / type → target column / type).
 */

@Data
public class JulySyncColumnVo011 {

    /** Source column. */
    @NotBlank(message = "sourceColumn is required")
    @Schema(description = "原表列", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceColumn;

    /** Source neutral type code. */
    @Schema(description = "原表列类型（中性类型 code，如 string/int/datetime）")
    private String sourceType;

    /** Target column. */
    @NotBlank(message = "targetColumn is required")
    @Schema(description = "目标列", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetColumn;

    /** Target neutral type code. */
    @Schema(description = "目标列类型（中性类型 code）")
    private String targetType;

    /** Optional value transform. */
    @Schema(description = "值转换（可选，如 trim）")
    private String transform;

    /** Sort order. */
    @Schema(description = "排序")
    private Integer sortOrder;
}
