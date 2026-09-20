package com.klsjnh.domain.aicenter.audio;

/*                AiAsrResult record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai asr result record
 *
 */

/**
 * Speech recognition result: the transcribed text plus an optional finish
 * reason.
 *
 * @param text         transcribed text
 * @param finishReason stop reason, nullable
 */

public record AiAsrResult(String text, String finishReason) {
}
