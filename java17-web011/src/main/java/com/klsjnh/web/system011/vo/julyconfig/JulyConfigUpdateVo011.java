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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
}
