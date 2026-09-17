package com.klsjnh.infrastructure.lowcode011.entity;

/*                JulyMetadataVersionPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  july metadata version po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * Publish snapshot persistence PO mapped to july_metadata_version.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_metadata_version")
public class JulyMetadataVersionPo extends BasePo011 {

    /** Low-code object name. */
    private String objectName;

    /** Version string. */
    private String version;

    /** MetaDTO snapshot JSON. */
    private String payloadJson;

    /** Produced physical table name. */
    private String physicalTable;

    /** Backup table name, nullable. */
    private String backupTable;

    /** Executed DDL text, nullable. */
    private String ddlText;

    /** Snapshot status (PENDING / PUBLISHED). */
    private String publishStatus;

    /** Publisher id, nullable. */
    private String publishedBy;

    /** Publish time, nullable while PENDING. */
    private LocalDateTime publishedAt;
}
