package com.klsjnh.web.system011.vo.julydictionary;

/*                JulyDictionaryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dictionary response VO (detail with items, and page rows).
 */

@Data
public class JulyDictionaryVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Dictionary code, unique, immutable. */
    @Schema(description = "字典编码（唯一，不可变）")
    private String dictionaryCode;

    /** Manual sort order. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;

    /** Dictionary display name. */
    @Schema(description = "字典名称")
    private String dictionaryName;

    /** Row status: 0 disabled / 1 enabled. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Creator. */
    @Schema(description = "创建人")
    private String createBy;

    /** Last modifier. */
    @Schema(description = "最后修改人")
    private String updateBy;

    /** Create time. */
    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    /** Update time. */
    @Schema(description = "最后修改日期")
    private LocalDateTime updateTime;

    /** Items (detail only, ordered by sort order). */
    @Schema(description = "字典项列表（仅详情返回，按排序升序）")
    private List<JulyDictionaryItemVo011> items;
}
