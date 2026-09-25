package com.klsjnh.application.aicenter.audio;

/*                AiAsrUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai asr use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.aicenter.AiCapabilityRegistry;
import com.klsjnh.application.aicenter.AiProviderResolver;
import com.klsjnh.domain.aicenter.audio.AiAsrPort;
import com.klsjnh.domain.aicenter.audio.AiAsrRequest;
import com.klsjnh.domain.aicenter.audio.AiAsrResult;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * AI speech recognition use case: resolves the provider / key by id or code,
 * dispatches to the asr capability port and returns the transcribed text. The
 * output is text, so no storage is involved.
 */

@ConditionalOnClass(name = "com.klsjnh.infrastructure.aicenter.media.AiMediaStore011")
@Service
public class AiAsrUseCase {

    /**
     * Provider / key resolver.
     */
    private final AiProviderResolver resolver;

    /**
     * Capability registry.
     */
    private final AiCapabilityRegistry registry;

    /**
     * Create the use case.
     *
     * @param resolver provider resolver
     * @param registry capability registry
     */
    public AiAsrUseCase(AiProviderResolver resolver, AiCapabilityRegistry registry) {
        this.resolver = resolver;
        this.registry = registry;
    }

    /**
     * Transcribe audio to text.
     *
     * @param target   routing / output header
     * @param audio    audio bytes, nullable when {@code audioUrl} is given
     * @param audioUrl audio url, nullable when {@code audio} is given
     * @param format   audio format, nullable
     * @param language language hint, nullable
     * @return recognition result, never null
     */
    public AiAsrResult recognize(AiInvokeTarget target, byte[] audio, String audioUrl, String format, String language) {
        if (target == null) {
            throw BusinessException.badRequest("target is required");
        }

        if ((audio == null || audio.length == 0) && (audioUrl == null || audioUrl.isBlank())) {
            throw BusinessException.badRequest("audio is required");
        }

        AiModelProvider provider = resolver.resolveProvider(target.providerCode(), target.providerId());
        AiModelProviderApi api = resolver.resolveApi(provider, target.keyCode(), target.keyId());
        String model = resolver.requireModel(provider, target.model());

        AiAsrPort port = registry.asrPort(provider.providerCode());

        if (port == null) {
            throw BusinessException.badRequest("no asr provider for: " + provider.providerCode());
        }

        AiAsrRequest request = new AiAsrRequest(provider.baseUrl(), api.apiKey(),
                new AiInvokeTarget(provider.providerCode(), provider.id().value(), target.keyCode(), target.keyId(),
                        model, target.returnType()),
                audio, audioUrl, format, language);

        return port.recognize(request);
    }
}
