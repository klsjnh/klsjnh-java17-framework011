package com.klsjnh.web.lowcode011.vo;

/*                JulyMetadataQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for object metadata.
 */

@Data
public class JulyMetadataQueryVo011 {

    /** Page index, 1 based. */
    @Schema(description = "页码（从 1 开始）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /** Object name keyword. */
    @Schema(description = "对象名关键字")
    private String keyword;

    /** Object type filter. */
    @Schema(description = "对象类型过滤")
    private String objectType;

    /** Status filter. */
    @Schema(description = "状态过滤（0 停用 / 1 启用）")
    private String status;
}
