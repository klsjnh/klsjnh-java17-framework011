package com.klsjnh.web.datasource.vo.julysync;

/*                JulySyncRuleVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync rule vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Sync rule response: source / target endpoints, business key and conflict.
 */

@Data
public class JulySyncRuleVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Sync code. */
    @Schema(description = "同步编码")
    private String syncCode;

    /** Sync name. */
    @Schema(description = "同步名称")
    private String syncName;

    /** Source datasource code. */
    @Schema(description = "源头数据源编码")
    private String sourceDsCode;

    /** Source kind. */
    @Schema(description = "源形态（sql / table / object）")
    private String sourceKind;

    /** Source data. */
    @Schema(description = "源数据（SQL / 表名 / 对象名）")
    private String sourceData;

    /** Target datasource code. */
    @Schema(description = "目标数据源编码")
    private String targetDsCode;

    /** Target kind. */
    @Schema(description = "目标形态（table / object）")
    private String targetKind;

    /** Target data. */
    @Schema(description = "目标数据（表名 / 对象名）")
    private String targetData;

    /** Mode. */
    @Schema(description = "模式（full / incr）")
    private String mode;

    /** Business key columns. */
    @Schema(description = "业务键（判重键，目标侧列名 CSV）")
    private String syncKey;

    /** Conflict strategy. */
    @Schema(description = "冲突策略（upsert / append）")
    private String conflict;

    /** Page size. */
    @Schema(description = "分页读取批大小")
    private Integer pageSize;

    /** Row status. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;
}
