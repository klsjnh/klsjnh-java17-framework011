package com.klsjnh.application.ai011;

/*                AiProviderRegistry class
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
 *
 */

import com.klsjnh.domain.ai011.capability.image.AiImagePort;
import com.klsjnh.domain.ai011.capability.tts.AiTtsPort;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Collects every AI capability port bean of the context (image / tts) and
 * resolves the one that supports a given provider code — the same registration
 * pattern as the message channel registry. A vendor is added by declaring one
 * more port bean; the platform is not modified.
 */

@Component
public class AiProviderRegistry {

    /**
     * Image capability ports.
     */
    private final List<AiImagePort> imagePorts;

    /**
     * Tts capability ports.
     */
    private final List<AiTtsPort> ttsPorts;

    /**
     * Create the registry from all capability port beans.
     *
     * @param imagePorts image ports
     * @param ttsPorts   tts ports
     */
    public AiProviderRegistry(List<AiImagePort> imagePorts, List<AiTtsPort> ttsPorts) {
        this.imagePorts = imagePorts;
        this.ttsPorts = ttsPorts;
    }

    /**
     * Resolve the image port for a provider code.
     *
     * @param providerCode provider code
     * @return port or null when unsupported
     */
    public AiImagePort imagePort(String providerCode) {
        for (AiImagePort port : imagePorts) {
            if (port.supports(providerCode)) {
                return port;
            }
        }

        return null;
    }

    /**
     * Resolve the tts port for a provider code.
     *
     * @param providerCode provider code
     * @return port or null when unsupported
     */
    public AiTtsPort ttsPort(String providerCode) {
        for (AiTtsPort port : ttsPorts) {
            if (port.supports(providerCode)) {
                return port;
            }
        }

        return null;
    }
}
