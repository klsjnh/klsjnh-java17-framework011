package com.klsjnh.web.aicenter.vo.aidomainprompt;

/*                JulyAiDomainPromptVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain prompt vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Prompt response VO (detail / page rows); the body is read via getContent.
 */

@Data
public class JulyAiDomainPromptVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Master domain id (pk_mt). */
    @Schema(description = "业务域 id（pk_mt）")
    private String pkMt;

    /** Prompt code, globally unique. */
    @Schema(description = "提示词编码（全局唯一）")
    private String promptCode;

    /** Prompt name. */
    @Schema(description = "提示词名称")
    private String promptName;

    /** Scene. */
    @Schema(description = "适用能力")
    private String scene;

    /** Content mode (inline / storage). */
    @Schema(description = "内容模式（inline / storage）")
    private String contentMode;

    /** Storage instance code. */
    @Schema(description = "存储实例（storage 时）")
    private String storageCode;

    /** Bucket. */
    @Schema(description = "桶（storage 时）")
    private String bucket;

    /** Object key. */
    @Schema(description = "对象 key（storage 时）")
    private String objectKey;

    /** Content hash. */
    @Schema(description = "内容 hash")
    private String contentHash;

    /** Content size in bytes. */
    @Schema(description = "字节数")
    private Long contentSize;

    /** Variable declarations. */
    @Schema(description = "变量声明")
    private String variables;

    /** Sort order. */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** Row status. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

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
