package com.klsjnh.web.aicenter.vo.aidomain;

/*                JulyAiDomainUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain update vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Domain update request (domainCode is immutable).
 */

@Data
public class JulyAiDomainUpdateVo011 {

    /** Domain id. */
    @NotBlank(message = "id is required")
    @Schema(description = "域 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Domain name. */
    @NotBlank(message = "domainName is required")
    @Schema(description = "域名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String domainName;

    /** Parent domain id, blank for root. */
    @Schema(description = "上级域 id（根为空串）")
    private String parentId;

    /** Sort order. */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Row status. */
    @Schema(description = "状态（0 停用 / 1 启用；留空保持）")
    private String status;
}
