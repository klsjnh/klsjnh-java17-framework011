package com.klsjnh.domain.aicenter.capability;

/*                AiCapabilityCodes class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  built-in ai capability codes
 *
 */

/**
 * Built-in AI capability codes. Plain string constants, not an enum: a new
 * capability is a new code plus a new port, without touching the registry.
 */

public final class AiCapabilityCodes {

    /** Text inference (chat / completion). */
    public static final String INFERENCE = "inference";

    /** Image generation (text-to-image / image-to-image). */
    public static final String IMAGE = "image";

    /** Speech synthesis. */
    public static final String TTS = "tts";

    /** Speech recognition. */
    public static final String ASR = "asr";

    private AiCapabilityCodes() {
    }
}
