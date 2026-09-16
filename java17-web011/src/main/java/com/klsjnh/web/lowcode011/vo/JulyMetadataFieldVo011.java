package com.klsjnh.web.lowcode011.vo;

/*                JulyMetadataFieldVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata field vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Field transport VO.
 */

@Data
public class JulyMetadataFieldVo011 {

    /** Field code. */
    @Schema(description = "字段编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldCode;

    /** Field name. */
    @Schema(description = "字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldName;

    /** Field type code. */
    @Schema(description = "字段类型（FieldType011）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldType;

    /** Field length. */
    @Schema(description = "长度")
    private Integer fieldLength;

    /** Required flag. */
    @Schema(description = "是否必填")
    private Boolean requiredField;

    /** Default value. */
    @Schema(description = "默认值")
    private String defaultValue;

    /** Sort order. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;
}
