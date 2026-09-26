package com.klsjnh.web.system011.vo.julyconfig;

/*                JulyConfigInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july config insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Insert request VO for a config entry (code immutable after create).
 */

@Data
public class JulyConfigInsertVo011 {

    /** Config key, unique, max 60, immutable after create. */
    @NotBlank(message = "code is required")
    @Schema(description = "配置项（唯一，最长 60，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /** Config value, max 300. */
    @NotBlank(message = "data is required")
    @Schema(description = "配置值（最长 300）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String data;

    /** Config status: '0' disabled / '1' enabled; null defaults to enabled. */
    @Schema(description = "状态（0 停用 / 1 启用；留空默认启用）")
    private String status;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}
