package com.klsjnh.web.datasource.vo.julysync;

/*                JulySyncInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync rule insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.util.List;

/**
 * Sync rule insert request: the rule header plus its column mapping.
 */

@Data
public class JulySyncInsertVo011 {

    /** Sync code. */
    @NotBlank(message = "syncCode is required")
    @Schema(description = "同步编码（全局唯一，不可变）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String syncCode;

    /** Sync name. */
    @NotBlank(message = "syncName is required")
    @Schema(description = "同步名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String syncName;

    /** Source datasource code. */
    @NotBlank(message = "sourceDsCode is required")
    @Schema(description = "源头数据源编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceDsCode;

    /** Source kind. */
    @Schema(description = "源形态（sql / table / object，默认 sql）")
    private String sourceKind;

    /** Source data. */
    @NotBlank(message = "sourceData is required")
    @Schema(description = "源数据（SQL 文本 / 表名 / 对象名）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceData;

    /** Target datasource code. */
    @NotBlank(message = "targetDsCode is required")
    @Schema(description = "目标数据源编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetDsCode;

    /** Target kind. */
    @Schema(description = "目标形态（table / object，默认 table）")
    private String targetKind;

    /** Target data. */
    @NotBlank(message = "targetData is required")
    @Schema(description = "目标数据（表名 / 对象名）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetData;

    /** Mode. */
    @Schema(description = "模式（full / incr，默认 full）")
    private String mode;

    /** Business key columns. */
    @NotBlank(message = "syncKey is required")
    @Schema(description = "业务键（判重键，目标侧列名 CSV）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String syncKey;

    /** Conflict strategy. */
    @Schema(description = "冲突策略（upsert / append，默认 upsert）")
    private String conflict;

    /** Page size. */
    @Schema(description = "分页读取批大小（默认 500）")
    private Integer pageSize;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Column mapping. */
    @NotNull(message = "columns is required")
    @Valid
    @Schema(description = "列对照（源列/源类型 → 目标列/目标类型）", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<JulySyncColumnVo011> columns;
}
