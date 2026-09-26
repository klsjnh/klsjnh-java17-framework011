package com.klsjnh.web.system011.vo.julydictionary;

/*                JulyDictionaryUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary update vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Update request VO for a dictionary type (dictionaryCode immutable).
 */

@Data
public class JulyDictionaryUpdateVo011 {

    /** Primary key. */
    @NotBlank(message = "id is required")
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Dictionary display name, max 100. */
    @NotBlank(message = "dictionaryName is required")
    @Schema(description = "字典名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictionaryName;

    /** Manual sort order, smaller comes first; blank keeps the stored one. */
    @Schema(description = "排序（越小越靠前，留空保持原值）")
    private Integer sortOrder;

    /** Row status: 0 disabled / 1 enabled; blank keeps the stored one. */
    @Schema(description = "状态（0 停用 / 1 启用，留空保持原值）")
    private String status;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}
