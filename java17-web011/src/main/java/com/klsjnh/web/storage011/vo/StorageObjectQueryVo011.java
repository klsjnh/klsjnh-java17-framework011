package com.klsjnh.web.storage011.vo;

/*                StorageObjectQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage object query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Object list / page request VO.
 */

@Data
public class StorageObjectQueryVo011 {

    /** Storage code, blank for the default instance. */
    @Schema(description = "存储编码（留空用默认实例）")
    private String storageCode;

    /** Bucket name, blank for the default bucket. */
    @Schema(description = "桶名（留空用默认桶）")
    private String bucketName;

    /** Key prefix. */
    @Schema(description = "对象前缀")
    private String prefix;

    /** Recursive flag (the adapter lists recursively). */
    @Schema(description = "是否递归（当前适配器恒递归）")
    private Boolean recursive;

    /** Page index, 1 based. */
    @Schema(description = "页码（从 1 开始）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;
}
