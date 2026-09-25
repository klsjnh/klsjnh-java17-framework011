package com.klsjnh.domain.aicenter.inference;

/*                AiInferencePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai chat port
 *
 */

import com.klsjnh.domain.aicenter.capability.AiCapabilityCodes;
import com.klsjnh.domain.aicenter.capability.AiCapabilityPort;

import java.util.stream.Stream;

/**
 * Inference (chat / completion) port: one resolved request in, one reply out —
 * or a stream of chunks. The OpenAI compatible adapter implements it; other
 * vendor protocols add adapters without changing callers.
 */

public interface AiInferencePort extends AiCapabilityPort {

    /** {@inheritDoc} */
    @Override
    default String capabilityCode() {
        return AiCapabilityCodes.INFERENCE;
    }

    /**
     * Send an inference request and return the whole reply.
     *
     * @param command resolved inference request
     * @return model reply, never null
     */
    AiInferenceResult chat(AiInferenceCommand command);

    /**
     * Send an inference request and stream the reply fragments.
     *
     * @param command resolved inference request
     * @return fragment stream, never null
     */
    Stream<AiInferenceChunk> stream(AiInferenceCommand command);
}
