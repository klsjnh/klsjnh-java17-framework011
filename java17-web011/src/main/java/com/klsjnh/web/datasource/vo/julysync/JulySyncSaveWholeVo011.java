package com.klsjnh.web.datasource.vo.julysync;

/*                JulySyncSaveWholeVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  sync rule save whole vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Whole save request (rule + column mappings): the rule is inserted when id is
 * blank and updated otherwise, then the column list replaces the old children.
 */

@Data
public class JulySyncSaveWholeVo011 {

    /** Rule id; blank inserts a new rule. */
    @Schema(description = "同步规则主键（留空=新增；非空=修改）")
    private String id;

    /** Sync code, unique, immutable; used on insert only. */
    @Schema(description = "同步编码（全局唯一，创建后不可修改；仅新增时用）")
    private String syncCode;

    /** Sync name, max 100. */
    @Schema(description = "同步名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String syncName;

    /** Source datasource code. */
    @Schema(description = "来源数据源编码")
    private String sourceDsCode;

    /** Source kind (sql / table). */
    @Schema(description = "来源形态（sql / table）")
    private String sourceKind;

    /** Source data (sql text or table name). */
    @Schema(description = "来源内容（SQL 文本或表名）")
    private String sourceData;

    /** Target datasource code. */
    @Schema(description = "目标数据源编码")
    private String targetDsCode;

    /** Target kind (sql / table). */
    @Schema(description = "目标形态（sql / table）")
    private String targetKind;

    /** Target data (table name). */
    @Schema(description = "目标内容（表名）")
    private String targetData;

    /** Sync mode. */
    @Schema(description = "同步模式")
    private String mode;

    /** Business key columns. */
    @Schema(description = "业务主键列")
    private String syncKey;

    /** Conflict strategy. */
    @Schema(description = "冲突策略")
    private String conflict;

    /** Page size. */
    @Schema(description = "分页大小")
    private Integer pageSize;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;

    /** Row status: 0 disabled / 1 enabled; blank keeps the stored one. */
    @Schema(description = "状态（0 停用 / 1 启用；留空保持）")
    private String status;

    /** Column mappings replacing the old children; blank means none. */
    @Schema(description = "列映射列表（整存替换：旧子表逻辑删 + 新列表插入）")
    private List<JulySyncColumnVo011> columns = new ArrayList<>();
}
