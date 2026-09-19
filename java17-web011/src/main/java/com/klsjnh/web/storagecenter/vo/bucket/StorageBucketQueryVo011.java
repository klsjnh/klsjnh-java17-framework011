package com.klsjnh.web.storagecenter.vo.bucket;

/*                StorageBucketQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage bucket query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Bucket list / page request VO.
 */

@Data
public class StorageBucketQueryVo011 {

    /** Storage code, blank for the default instance. */
    @Schema(description = "存储编码（留空用默认实例）")
    private String storageCode;

    /** Page index, 1 based. */
    @Schema(description = "页码（从 1 开始）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /** Name keyword. */
    @Schema(description = "桶名关键字")
    private String keyword;
}
