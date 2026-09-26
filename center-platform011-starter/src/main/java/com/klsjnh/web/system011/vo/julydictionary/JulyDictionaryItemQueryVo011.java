package com.klsjnh.web.system011.vo.julydictionary;

/*                JulyDictionaryItemQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary item query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Item list request VO: items of one dictionary, optional status filter.
 */

@Data
public class JulyDictionaryItemQueryVo011 {

    /** Dictionary code. */
    @NotBlank(message = "dictionaryCode is required")
    @Schema(description = "字典编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictionaryCode;

    /** Row status filter, blank for all. */
    @Schema(description = "状态过滤（0 停用 / 1 启用，留空为全部）")
    private String status;
}
