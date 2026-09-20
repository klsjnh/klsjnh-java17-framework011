package com.klsjnh.domain.aicenter.audio;

/*                AiTtsPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai tts capability SPI
 *
 */

import com.klsjnh.domain.aicenter.capability.AiCapabilityCodes;
import com.klsjnh.domain.aicenter.capability.AiCapabilityPort;
import com.klsjnh.domain.aicenter.capability.AiMedia;

/**
 * SPI: a speech synthesis provider. Implementations are collected by the AI
 * capability registry (Spring {@code List} injection); a vendor is added by
 * declaring one more bean. Provider codes are an open string vocabulary.
 */

public interface AiTtsPort extends AiCapabilityPort {

    /** {@inheritDoc} */
    @Override
    default String capabilityCode() {
        return AiCapabilityCodes.TTS;
    }

    /**
     * Whether this provider serves the given provider code.
     *
     * @param providerCode provider code
     * @return true when supported
     */
    boolean supports(String providerCode);

    /**
     * Synthesize speech.
     *
     * @param request vendor-neutral request
     * @return generated audio artifact, never null
     */
    AiMedia synthesize(AiTtsRequest request);
}
