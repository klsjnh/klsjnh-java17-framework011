package com.klsjnh.web.aicenter.vo.aifile;

/*                AiFileContentVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file content response vo
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Provider file content response (base64).
 */

@Data
public class AiFileContentVo011 {

    /** Provider file id. */
    @Schema(description = "厂商文件 id")
    private String fileId;

    /** Size in bytes. */
    @Schema(description = "文件大小（字节）")
    private Long bytes;

    /** Content as base64. */
    @Schema(description = "文件内容（base64）")
    private String base64;
}
