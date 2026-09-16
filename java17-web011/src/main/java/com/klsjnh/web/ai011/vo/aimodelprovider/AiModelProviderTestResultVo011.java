package com.klsjnh.web.ai011.vo.aimodelprovider;

/*                AiModelProviderTestResultVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider test result vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Connectivity probe response VO (HTTP always 200, result in the payload).
 */

@Data
public class AiModelProviderTestResultVo011 {

    /** True when the endpoint answered a 2xx. */
    @Schema(description = "是否连通（2xx 为 true）")
    private boolean success;

    /** Safe human readable detail (never contains the key). */
    @Schema(description = "结果说明（不含密钥）")
    private String message;

    /** Http status when a response was received, else null. */
    @Schema(description = "HTTP 状态码（未收到响应为 null）")
    private Integer httpStatus;
}
