package com.klsjnh.web.lowcode011.vo;

/*                JulyMetadataVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Object metadata response VO with its three child collections.
 */

@Data
public class JulyMetadataVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Object name, unique, immutable. */
    @Schema(description = "对象名（唯一，不可变）")
    private String objectName;

    /** Sort order. */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** Object type code. */
    @Schema(description = "对象类型（ObjectType011）")
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

    /** Row status. */
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

    /** Fields. */
    @Schema(description = "字段列表")
    private List<JulyMetadataFieldVo011> fields;

    /** Displays. */
    @Schema(description = "显示列列表")
    private List<JulyMetadataDisplayVo011> displays;

    /** Services. */
    @Schema(description = "服务列表")
    private List<JulyMetadataServiceVo011> services;
}
