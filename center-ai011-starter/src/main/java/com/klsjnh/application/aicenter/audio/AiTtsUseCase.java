package com.klsjnh.application.aicenter.audio;

/*                AiTtsUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai tts use case class
 *      2026.09.20  local-storage gate + media persistence
 *      2026.09.24  call-time storage locator; drop local gate / defaults
 *      2026.09.26  explicit permission checks (aiAudio)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.aicenter.AiCapabilityRegistry;
import com.klsjnh.application.aicenter.AiProviderResolver;
import com.klsjnh.domain.aicenter.audio.AiAudioPermissionCodes011;
import com.klsjnh.domain.aicenter.audio.AiTtsPort;
import com.klsjnh.domain.aicenter.audio.AiTtsRequest;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.capability.AiMedia;
import com.klsjnh.domain.aicenter.media.AiMediaLocation;
import com.klsjnh.domain.aicenter.media.AiMediaRef;
import com.klsjnh.domain.aicenter.media.AiMediaStorePort;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.iam.auth.AuthorizationPort;

import org.springframework.stereotype.Service;

/**
 * AI speech synthesis use case: resolves the provider / key by id or code,
 * dispatches to the tts capability port, then persists the audio at the
 * caller-supplied storage location and returns a reference. The consumer owns
 * the artifact lifecycle. No pre-bound default storage.
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
    private final AiCapabilityRegistry registry;

    /**
     * Media store port.
     */
    private final AiMediaStorePort mediaStore;

    /**
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * Create the use case.
     *
     * @param resolver           provider resolver
     * @param registry           capability registry
     * @param mediaStore         media store
     * @param authorizationPort  authorization port
     */
    public AiTtsUseCase(AiProviderResolver resolver, AiCapabilityRegistry registry, AiMediaStorePort mediaStore,
            AuthorizationPort authorizationPort) {
        this.resolver = resolver;
        this.registry = registry;
        this.mediaStore = mediaStore;
        this.authorizationPort = authorizationPort;
    }

    /**
     * Synthesize speech and persist it at the given storage location.
     *
     * @param target      routing / output header
     * @param input       text to synthesize
     * @param voice       voice id / name, nullable
     * @param instruction style / emotion instruction, nullable
     * @param speed       speed multiplier, nullable
     * @param volume      volume multiplier, nullable
     * @param format      audio format, nullable
     * @param sampleRate  sample rate, nullable
     * @param location    storage instance + bucket locator (required)
     * @return persisted media reference, never null
     */
    public AiMediaRef synthesize(String operatorId, AiInvokeTarget target, String input, String voice,
            String instruction, Double speed, Double volume, String format, Integer sampleRate,
            AiMediaLocation location) {
        authorizationPort.assertHas(operatorId, AiAudioPermissionCodes011.SYNTHESIZE);

        if (target == null) {
            throw BusinessException.badRequest("target is required");
        }

        if (StringUtil011.isBlank(input)) {
            throw BusinessException.badRequest("input is required");
        }

        if (location == null) {
            throw BusinessException.badRequest("storage location is required");
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

        AiMedia media = port.synthesize(request);

        return mediaStore.save(media, location);
    }
}
