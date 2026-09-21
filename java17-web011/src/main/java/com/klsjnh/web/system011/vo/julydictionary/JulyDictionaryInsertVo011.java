package com.klsjnh.web.system011.vo.julydictionary;

/*                JulyDictionaryInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Insert request VO for a dictionary type.
 */

@Data
public class JulyDictionaryInsertVo011 {

    /** Dictionary code, unique, immutable, max 60. */
    @Schema(description = "字典编码（唯一，最长 60，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictionaryCode;

    /** Manual sort order, smaller comes first; blank falls back to the default. */
    @Schema(description = "排序（越小越靠前，留空取默认 9999）")
    private Integer sortOrder;

    /** Dictionary display name, max 100. */
    @Schema(description = "字典名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictionaryName;

    /** Row status: 0 disabled / 1 enabled; blank falls back to enabled. */
    @Schema(description = "状态（0 停用 / 1 启用，留空默认启用）")
    private String status;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}
