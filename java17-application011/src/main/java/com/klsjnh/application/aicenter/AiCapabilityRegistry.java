package com.klsjnh.application.aicenter;

/*                AiCapabilityRegistry class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai provider registry class
 *      2026.09.20  capability-code routing (inference/image/tts/asr)
 *
 */

import com.klsjnh.domain.aicenter.audio.AiAsrPort;
import com.klsjnh.domain.aicenter.audio.AiTtsPort;
import com.klsjnh.domain.aicenter.capability.AiCapabilityCodes;
import com.klsjnh.domain.aicenter.capability.AiCapabilityPort;
import com.klsjnh.domain.aicenter.image.AiImagePort;
import com.klsjnh.domain.aicenter.inference.AiInferencePort;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects every {@link AiCapabilityPort} bean of the context and resolves the
 * one serving a given (capability, provider) pair — the same registration
 * pattern as the message channel registry. A new capability or vendor is a new
 * port bean; the registry is not modified.
 */

@Component
public class AiCapabilityRegistry {

    /**
     * Ports grouped by capability code.
     */
    private final Map<String, List<AiCapabilityPort>> byCapability = new HashMap<>();

    /**
     * Create the registry from all capability port beans.
     *
     * @param ports capability ports
     */
    public AiCapabilityRegistry(List<AiCapabilityPort> ports) {
        for (AiCapabilityPort port : ports) {
            byCapability.computeIfAbsent(port.capabilityCode(), key -> new ArrayList<>()).add(port);
        }
    }

    /**
     * Resolve the port serving a capability / provider pair.
     *
     * @param capabilityCode capability code
     * @param providerCode   provider code
     * @return port or null when unsupported
     */
    public AiCapabilityPort port(String capabilityCode, String providerCode) {
        for (AiCapabilityPort port : byCapability.getOrDefault(capabilityCode, List.of())) {
            if (port.supports(providerCode)) {
                return port;
            }
        }

        return null;
    }

    /**
     * Resolve the inference port for a provider code.
     *
     * @param providerCode provider code
     * @return port or null when unsupported
     */
    public AiInferencePort inferencePort(String providerCode) {
        return (AiInferencePort) port(AiCapabilityCodes.INFERENCE, providerCode);
    }

    /**
     * Resolve the image port for a provider code.
     *
     * @param providerCode provider code
     * @return port or null when unsupported
     */
    public AiImagePort imagePort(String providerCode) {
        return (AiImagePort) port(AiCapabilityCodes.IMAGE, providerCode);
    }

    /**
     * Resolve the tts port for a provider code.
     *
     * @param providerCode provider code
     * @return port or null when unsupported
     */
    public AiTtsPort ttsPort(String providerCode) {
        return (AiTtsPort) port(AiCapabilityCodes.TTS, providerCode);
    }

    /**
     * Resolve the asr port for a provider code.
     *
     * @param providerCode provider code
     * @return port or null when unsupported
     */
    public AiAsrPort asrPort(String providerCode) {
        return (AiAsrPort) port(AiCapabilityCodes.ASR, providerCode);
    }
}
