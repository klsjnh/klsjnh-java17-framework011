package com.klsjnh.web.platform011.export;

/*                PlatformExportVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.06
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.06  generic platform export request
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Generic platform export request: one registered object code and the payload
 * format. The response is a streamed download (not the response envelope) for
 * every format.
 */

@Data
public class PlatformExportVo011 {

    /** Registered export object code. */
    @NotBlank(message = "objectCode is required")
    @Schema(description = "导出对象编码（已注册 ExportProvider）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectCode;

    /** Payload format: json / xlsx / csv. */
    @NotBlank(message = "format is required")
    @Pattern(regexp = "json|xlsx|csv", message = "format must be json / xlsx / csv")
    @Schema(description = "导出格式（json / xlsx / csv，均为流式下载）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String format;
}
