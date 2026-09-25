package com.klsjnh.domain.aicenter.audio;

/*                AiAsrRequest record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai asr request record
 *
 */

import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;

/**
 * One speech recognition request, fully resolved by the application layer:
 * endpoint + key + model in {@code target}, the audio beside it. The audio is
 * supplied as bytes (downloaded by the caller) or as a url. Vendor-neutral.
 *
 * @param baseUrl  provider base url (e.g. https://api.stepfun.com/v1)
 * @param apiKey   api key secret, never echoed back
 * @param target   routing / output header (provider code / model)
 * @param audio    audio bytes, nullable when {@code audioUrl} is given
 * @param audioUrl audio url, nullable when {@code audio} is given
 * @param format   audio format (mp3 / wav / pcm ...), nullable for provider default
 * @param language language hint, nullable
 */

public record AiAsrRequest(String baseUrl, String apiKey, AiInvokeTarget target, byte[] audio, String audioUrl,
        String format, String language) {

    /**
     * Whether audio bytes are present.
     *
     * @return true when bytes are present
     */
    public boolean hasAudio() {
        return (audio != null && audio.length > 0) || (audioUrl != null && !audioUrl.isBlank());
    }
}
