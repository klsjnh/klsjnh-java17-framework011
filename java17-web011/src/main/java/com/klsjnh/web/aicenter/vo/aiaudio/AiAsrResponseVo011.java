package com.klsjnh.web.aicenter.vo.aiaudio;

/*                AiAsrResponseVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai asr response vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Speech recognition response: the transcribed text.
 */

@Data
public class AiAsrResponseVo011 {

    /** Transcribed text. */
    @Schema(description = "识别出的文字")
    private String text;

    /** Stop reason, nullable. */
    @Schema(description = "结束原因（可空）")
    private String finishReason;
}
