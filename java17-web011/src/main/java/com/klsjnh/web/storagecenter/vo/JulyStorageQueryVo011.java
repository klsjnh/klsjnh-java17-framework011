package com.klsjnh.web.storagecenter.vo;

/*                JulyStorageQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for storage instances.
 */

@Data
public class JulyStorageQueryVo011 {

    /** Page index, 1 based. */
    @Schema(description = "页码（从 1 开始）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /** Keyword matched against code / name. */
    @Schema(description = "关键字（编码 / 名称 模糊）")
    private String keyword;

    /** Provider filter. */
    @Schema(description = "存储类型过滤")
    private String provider;

    /** Status filter. */
    @Schema(description = "状态过滤（0 停用 / 1 启用）")
    private String status;
}
