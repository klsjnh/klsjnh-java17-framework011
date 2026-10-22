package com.klsjnh.web.aicenter.vo.aifile;

/*                AiFileVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file metadata response vo
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Provider file metadata response.
 */

@Data
public class AiFileVo011 {

    /** Provider code. */
    @Schema(description = "提供方编码")
    private String providerCode;

    /** Provider file id. */
    @Schema(description = "厂商文件 id")
    private String fileId;

    /** File name. */
    @Schema(description = "文件名")
    private String fileName;

    /** Size in bytes. */
    @Schema(description = "文件大小（字节）")
    private Long bytes;

    /** Purpose. */
    @Schema(description = "用途")
    private String purpose;

    /** Provider status. */
    @Schema(description = "厂商状态")
    private String status;

    /** Creation epoch seconds. */
    @Schema(description = "创建时间（epoch 秒）")
    private Long createdAt;
}
