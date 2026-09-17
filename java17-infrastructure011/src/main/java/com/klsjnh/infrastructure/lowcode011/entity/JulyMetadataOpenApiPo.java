package com.klsjnh.infrastructure.lowcode011.entity;

/*                JulyMetadataOpenApiPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  july metadata open api po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * Open API configuration persistence PO mapped to july_metadata_open_api.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_metadata_open_api")
public class JulyMetadataOpenApiPo extends BasePo011 {

    /** Low-code object name. */
    private String objectName;

    /** Whether the open API is enabled ('0' / '1'). */
    private String enabled;

    /** Auth mode (closed / none / apiKey). */
    private String authMode;

    /** Allowed operations (comma list). */
    private String allowedOps;

    /** Api key (masked on output). */
    private String apiKey;

    /** Last rotation time. */
    private LocalDateTime apiKeyUpdatedAt;
}
