package com.klsjnh.web.storage011.vo;

/*                JulyStorageVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Storage instance response VO.
 * <p>
 * There is deliberately NO {@code secretKey} field: the secret is write-only,
 * keeping it out of the type is the strongest guarantee it is never echoed back.
 * </p>
 */

@Data
public class JulyStorageVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Storage code, unique, immutable. */
    @Schema(description = "存储编码（唯一，不可变）")
    private String storageCode;

    /** Manual sort order. */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** Display name. */
    @Schema(description = "存储名称")
    private String storageName;

    /** Storage type code. */
    @Schema(description = "存储类型")
    private String provider;

    /** Local root directory. */
    @Schema(description = "本地根目录")
    private String basePath;

    /** Endpoint. */
    @Schema(description = "Endpoint")
    private String endpoint;

    /** Access key. */
    @Schema(description = "Access Key")
    private String accessKey;

    /** Whether to use HTTPS. */
    @Schema(description = "是否 HTTPS")
    private Boolean secure;

    /** Default bucket. */
    @Schema(description = "默认桶")
    private String defaultBucket;

    /** Presigned URL expiry seconds. */
    @Schema(description = "预签名有效期（秒）")
    private Integer presignExpirySeconds;

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
}
