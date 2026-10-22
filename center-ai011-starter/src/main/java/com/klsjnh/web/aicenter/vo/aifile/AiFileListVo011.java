package com.klsjnh.web.aicenter.vo.aifile;

/*                AiFileListVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider file list request vo
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Provider file list request.
 */

@Data
public class AiFileListVo011 {

    /** Provider code, alternative to providerId. */
    @Schema(description = "模型提供方编码（与 providerId 二选一）")
    private String provider;

    /** Provider id, alternative to provider. */
    @Schema(description = "模型提供方 id（与 provider 二选一）")
    private String providerId;

    /** Api key code; blank for the default enabled key. */
    @Schema(description = "密钥编码（留空用默认启用密钥）")
    private String api;

    /** Api key id, alternative to api. */
    @Schema(description = "密钥 id（与 api 二选一）")
    private String apiId;

    /** Purpose filter, blank for all. */
    @Schema(description = "用途过滤（留空 = 全部）")
    private String purpose;
}
