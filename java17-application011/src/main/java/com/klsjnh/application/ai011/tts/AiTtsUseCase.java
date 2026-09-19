package com.klsjnh.application.ai011.tts;

/*                AiTtsUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai tts use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.ai011.AiProviderRegistry;
import com.klsjnh.application.ai011.AiProviderResolver;
import com.klsjnh.domain.ai011.capability.AiInvokeTarget;
import com.klsjnh.domain.ai011.capability.AiMedia;
import com.klsjnh.domain.ai011.capability.tts.AiTtsPort;
import com.klsjnh.domain.ai011.capability.tts.AiTtsRequest;
import com.klsjnh.domain.ai011.modelprovider.AiModelProvider;
import com.klsjnh.domain.ai011.modelprovider.AiModelProviderApi;

import org.springframework.stereotype.Service;

/**
 * AI speech synthesis use case: resolves the provider / key by id or code, then
 * dispatches to the tts capability port that supports the provider.
 */

@Service
public class AiTtsUseCase {

    /**
     * Provider / key resolver.
     */
    private final AiProviderResolver resolver;

    /**
     * Capability registry.
     */
    private final AiProviderRegistry registry;

    /**
     * Create the use case.
     *
     * @param resolver provider resolver
     * @param registry capability registry
     */
    public AiTtsUseCase(AiProviderResolver resolver, AiProviderRegistry registry) {
        this.resolver = resolver;
        this.registry = registry;
    }

    /**
     * Synthesize speech from text.
     *
     * @param target      routing / output header
     * @param input       text to synthesize
     * @param voice       voice id / name, nullable
     * @param instruction style / emotion instruction, nullable
     * @param speed       speed multiplier, nullable
     * @param volume      volume multiplier, nullable
     * @param format      audio format, nullable
     * @param sampleRate  sample rate, nullable
     * @return generated audio artifact, never null
     */
    public AiMedia synthesize(AiInvokeTarget target, String input, String voice, String instruction, Double speed,
            Double volume, String format, Integer sampleRate) {
        if (target == null) {
            throw BusinessException.badRequest("target is required");
        }

        if (StringUtil011.isBlank(input)) {
            throw BusinessException.badRequest("input is required");
        }

        AiModelProvider provider = resolver.resolveProvider(target.providerCode(), target.providerId());
        AiModelProviderApi api = resolver.resolveApi(provider, target.keyCode(), target.keyId());
        String model = resolver.requireModel(provider, target.model());

        AiTtsPort port = registry.ttsPort(provider.providerCode());

        if (port == null) {
            throw BusinessException.badRequest("no tts provider for: " + provider.providerCode());
        }

        AiTtsRequest request = new AiTtsRequest(provider.baseUrl(), api.apiKey(),
                new AiInvokeTarget(provider.providerCode(), provider.id().value(), target.keyCode(), target.keyId(),
                        model, target.returnType()),
                input, voice, instruction, speed, volume, format, sampleRate);

        return port.synthesize(request);
    }
}
