package com.klsjnh.web.storagecenter.vo.bucket;

/*                StorageBucketVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  storage bucket read vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Bucket row response VO (the july_storage_provider_bucket read model).
 */

@Data
public class StorageBucketVo011 {

    /** Bucket code, unique within the instance, immutable. */
    @Schema(description = "桶编码（同实例内唯一，不可变）")
    private String bucketCode;

    /** Bucket name. */
    @Schema(description = "桶名称")
    private String bucketName;

    /** Whether this is the default bucket. */
    @Schema(description = "是否默认桶")
    private Boolean isDefault;

    /** Manual sort order. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;

    /** Row status: 0 disabled / 1 enabled. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Creator. */
    @Schema(description = "创建人")
    private String createdBy;

    /** Last modifier. */
    @Schema(description = "最后修改人")
    private String updatedBy;

    /** Create date. */
    @Schema(description = "创建日期")
    private LocalDateTime createDate;

    /** Last modify date. */
    @Schema(description = "最后修改日期")
    private LocalDateTime modifyDate;
}
