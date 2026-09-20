package com.klsjnh.domain.aicenter.audio;

/*                AiAsrPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai asr capability SPI
 *
 */

import com.klsjnh.domain.aicenter.capability.AiCapabilityCodes;
import com.klsjnh.domain.aicenter.capability.AiCapabilityPort;

/**
 * SPI: a speech recognition provider. Implementations are collected by the AI
 * capability registry (Spring {@code List} injection); a vendor is added by
 * declaring one more bean. Provider codes are an open string vocabulary.
 */

public interface AiAsrPort extends AiCapabilityPort {

    /** {@inheritDoc} */
    @Override
    default String capabilityCode() {
        return AiCapabilityCodes.ASR;
    }

    /**
     * Transcribe audio to text.
     *
     * @param request vendor-neutral request
     * @return recognition result, never null
     */
    AiAsrResult recognize(AiAsrRequest request);
}
