package com.klsjnh.domain.ai011.capability.tts;

/*                AiTtsRequest record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai tts request record
 *
 */

import com.klsjnh.domain.ai011.capability.AiInvokeTarget;

/**
 * One speech synthesis (text-to-speech) request, fully resolved by the
 * application layer: endpoint + key + model in {@code target}, the capability
 * parameters beside it. Vendor-neutral; a provider maps these to its wire
 * fields.
 *
 * @param baseUrl     provider base url (e.g. https://api.stepfun.com/v1)
 * @param apiKey      api key secret, never echoed back
 * @param target      routing / output header (provider code / model / returnType)
 * @param input       text to synthesize, required (max ~1000 chars per vendor)
 * @param voice       voice id / name, nullable for the provider default
 * @param instruction natural-language style / emotion instruction, nullable
 * @param speed       speaking speed multiplier, nullable
 * @param volume      volume multiplier, nullable
 * @param format      audio format (mp3 / wav / pcm ...), nullable for default
 * @param sampleRate  sample rate in Hz, nullable for default
 */

public record AiTtsRequest(String baseUrl, String apiKey, AiInvokeTarget target, String input, String voice,
        String instruction, Double speed, Double volume, String format, Integer sampleRate) {
}
