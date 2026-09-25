package com.klsjnh.web.system011.vo.julydictionary;

/*                JulyDictionaryItemVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary item vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dictionary item response VO.
 */

@Data
public class JulyDictionaryItemVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Item code, unique within the dictionary. */
    @Schema(description = "字典项编码（字典内唯一）")
    private String itemCode;

    /** Item display name. */
    @Schema(description = "字典项名称")
    private String itemLabel;

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
