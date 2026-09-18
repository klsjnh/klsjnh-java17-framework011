package com.klsjnh.web.lowcode011.vo.julybusinessmodeling;

/*                JulyBusinessModelingInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Insert request VO for a modeling entry.
 */

@Data
public class JulyBusinessModelingInsertVo011 {

    /** Modeling code, unique, immutable, max 60. */
    @Schema(description = "建模编码（唯一，最长 60，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String modelCode;

    /** Modeling name, max 100. */
    @Schema(description = "建模名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String modelName;

    /** Datasource code, must be enabled. */
    @Schema(description = "数据源编码（须存在且启用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dataSourceCode;

    /** Take-out SQL, optional, must be a read-only SELECT when present. */
    @Schema(description = "取数 SQL（可空；非空须为只读 SELECT）")
    private String sqlContent;

    /** Object name, unique, immutable, max 60. */
    @Schema(description = "低代码对象名（唯一，最长 60，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectName;

    /** Object type code. */
    @Schema(description = "对象类型（type011/type013/type_tree/type_tree011/type021）")
    private String objectType;

    /** Object description. */
    @Schema(description = "对象描述")
    private String objectDescription;

    /** Target package name. */
    @Schema(description = "目标包名")
    private String packageName;

    /** Import / export business field list. */
    @Schema(description = "参与导入导出的业务字段清单（逗号分隔）")
    private String businessField;

    /** Front-end route path. */
    @Schema(description = "前端路由")
    private String routerPath;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;

    /** Field definitions (typically from a probe). */
    @Schema(description = "字段定义（通常来自探针推断）")
    private List<JulyBusinessModelingFieldVo011> fieldData;
}
