package com.klsjnh.web.dataservice011.vo.julybusinessmodeling;

/*                JulyBusinessModelingFieldVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling field vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Field definition transport (metaData fieldData item).
 */

@Data
public class JulyBusinessModelingFieldVo011 {

    /** Field code (snake_case). */
    @Schema(description = "字段编码（snake_case）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /** Field display name. */
    @Schema(description = "字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /** Field type code (FieldType011). */
    @Schema(description = "字段类型（id/status/create_by/update_by/create_time/update_time/string/int/float/date/boolean/text）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldType;

    /** Field length. */
    @Schema(description = "长度（非长度类型为 0）")
    private Integer length;

    /** Whether the field is required. */
    @Schema(description = "是否必填")
    private Boolean notNull;

    /** Default value. */
    @Schema(description = "默认值")
    private String defaultValue;
}
