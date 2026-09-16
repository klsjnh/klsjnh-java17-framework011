package com.klsjnh.web.lowcode011.vo;

/*                JulyMetadataSaveVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata save vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Insert / update request VO for an object metadata (master + three child
 * collections).
 */

@Data
public class JulyMetadataSaveVo011 {

    /** Primary key, null on insert. */
    @Schema(description = "主键（新增不传，修改必传）")
    private String id;

    /** Object name, unique, immutable. */
    @Schema(description = "对象名（唯一，不可变）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectName;

    /** Sort order. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;

    /** Object type code. */
    @Schema(description = "对象类型（type011/type013/type_tree/type_tree011/type021）")
    private String objectType;

    /** Object description. */
    @Schema(description = "对象描述")
    private String description;

    /** Business field mapping. */
    @Schema(description = "业务字段清单")
    private String businessField;

    /** Target package name. */
    @Schema(description = "目标包名")
    private String packageName;

    /** Route path. */
    @Schema(description = "前端路由")
    private String routerPath;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Row status (update). */
    @Schema(description = "状态（0 停用 / 1 启用，修改可传）")
    private String status;

    /** Fields (replace all). */
    @Schema(description = "字段列表（整体替换）")
    private List<JulyMetadataFieldVo011> fields;

    /** Displays (replace all). */
    @Schema(description = "显示列列表（整体替换）")
    private List<JulyMetadataDisplayVo011> displays;

    /** Services (replace all). */
    @Schema(description = "服务列表（整体替换）")
    private List<JulyMetadataServiceVo011> services;
}
