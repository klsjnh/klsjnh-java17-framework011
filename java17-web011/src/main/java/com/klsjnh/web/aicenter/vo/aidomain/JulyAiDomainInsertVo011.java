package com.klsjnh.web.aicenter.vo.aidomain;

/*                JulyAiDomainInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Domain insert request.
 */

@Data
public class JulyAiDomainInsertVo011 {

    /** Domain code, globally unique, immutable. */
    @Schema(description = "域编码（全局唯一，不可变）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String domainCode;

    /** Domain name. */
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
}
