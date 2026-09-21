package com.klsjnh.web.system011.vo.julydictionary;

/*                JulyDictionaryItemInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary item insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Insert request VO for a dictionary item (dictionaryCode locates the master).
 */

@Data
public class JulyDictionaryItemInsertVo011 {

    /** Dictionary code locating the master. */
    @Schema(description = "字典编码（定位主表）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictionaryCode;

    /** Manual sort order, smaller comes first; blank falls back to the default. */
    @Schema(description = "排序（越小越靠前，留空取默认 9999）")
    private Integer sortOrder;

    /** Item code, unique within the dictionary, max 60. */
    @Schema(description = "字典项编码（字典内唯一，最长 60，创建后不可修改）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String itemCode;

    /** Item display name, max 100. */
    @Schema(description = "字典项名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String itemLabel;

    /** Row status: 0 disabled / 1 enabled; blank falls back to enabled. */
    @Schema(description = "状态（0 停用 / 1 启用，留空默认启用）")
    private String status;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}
