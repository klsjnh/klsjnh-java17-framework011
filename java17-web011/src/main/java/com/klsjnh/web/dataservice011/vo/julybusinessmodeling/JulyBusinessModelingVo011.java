package com.klsjnh.web.dataservice011.vo.julybusinessmodeling;

/*                JulyBusinessModelingVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Modeling response VO (detail and page rows), carrying the product
 * (metaData + fieldData) and the editable SQL text.
 */

@Data
public class JulyBusinessModelingVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Modeling code, unique, immutable. */
    @Schema(description = "建模编码（唯一，不可变）")
    private String modelCode;

    /** Modeling name. */
    @Schema(description = "建模名称")
    private String modelName;

    /** Low-code object name. */
    @Schema(description = "低代码对象名")
    private String objectName;

    /** Datasource code. */
    @Schema(description = "数据源编码")
    private String dataSourceCode;

    /** Take-out SQL (returned so the management page can edit it). */
    @Schema(description = "取数 SQL")
    private String sqlContent;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Row status: 0 disabled / 1 enabled. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;

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

    /** Hand-over product (metaData + fieldData). */
    @Schema(description = "产物（metaData + fieldData）")
    private JulyBusinessModelingMetaVo011 metaData;
}
