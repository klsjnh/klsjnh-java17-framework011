package com.klsjnh.web.system011.vo.julyconfig;

/*                JulyConfigUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config update vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Update request VO for a config entry (code immutable).
 */

@Data
public class JulyConfigUpdateVo011 {

    /** Primary key. */
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Config value, max 300. */
    @Schema(description = "配置值（最长 300）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String data;

    /** Config status: '0' disabled / '1' enabled; null keeps the stored one. */
    @Schema(description = "状态（0 停用 / 1 启用；留空保持）")
    private String status;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}
