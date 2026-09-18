package com.klsjnh.web.lowcode011.vo.julybusinessmodeling;

/*                JulyBusinessModelingMetaVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling meta vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Hand-over product VO: the object metadata plus its field list.
 */

@Data
public class JulyBusinessModelingMetaVo011 {

    /** Object name (table name), globally unique, immutable. */
    @Schema(description = "低代码对象名（表名，全局唯一，不可变）")
    private String objectName;

    /** Object description. */
    @Schema(description = "对象描述")
    private String description;

    /** Object type code (ObjectType011). */
    @Schema(description = "对象类型（type011/type013/type_tree/type_tree011/type021）")
    private String objectType;

    /** Target package name. */
    @Schema(description = "目标包名")
    private String packageName;

    /** Import / export business field list (comma separated). */
    @Schema(description = "参与导入导出的业务字段清单（逗号分隔）")
    private String importField;

    /** Front-end route path. */
    @Schema(description = "前端路由")
    private String url;

    /** Field definitions. */
    @Schema(description = "字段定义列表")
    private List<JulyBusinessModelingFieldVo011> fieldData;
}
