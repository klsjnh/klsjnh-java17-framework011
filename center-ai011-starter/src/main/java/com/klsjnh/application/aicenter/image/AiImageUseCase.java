package com.klsjnh.application.aicenter.image;

/*                AiImageUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai image use case class
 *      2026.09.20  local-storage gate + media persistence
 *      2026.09.24  call-time storage locator; drop local gate / defaults
 *      2026.09.26  explicit permission checks (aiImage)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.aicenter.AiCapabilityRegistry;
import com.klsjnh.application.aicenter.AiProviderResolver;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.capability.AiMedia;
import com.klsjnh.domain.aicenter.image.AiImagePermissionCodes011;
import com.klsjnh.domain.aicenter.image.AiImagePort;
import com.klsjnh.domain.aicenter.image.AiImageRequest;
import com.klsjnh.domain.aicenter.media.AiMediaLocation;
import com.klsjnh.domain.aicenter.media.AiMediaRef;
import com.klsjnh.domain.aicenter.media.AiMediaStorePort;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.iam.auth.AuthorizationPort;

import org.springframework.stereotype.Service;

/**
 * AI image generation use case: resolves the provider / key by id or code,
 * dispatches to the capability port (text-to-image or image-to-image, chosen by
 * the presence of an input image), then persists the artifact at the
 * caller-supplied storage location and returns a reference. The consumer owns
 * the artifact lifecycle. No pre-bound default storage.
 */

@Service
public class AiImageUseCase {

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
    public AiImageUseCase(AiProviderResolver resolver, AiCapabilityRegistry registry, AiMediaStorePort mediaStore,
            AuthorizationPort authorizationPort) {
        this.resolver = resolver;
        this.registry = registry;
        this.mediaStore = mediaStore;
        this.authorizationPort = authorizationPort;
    }

    /**
     * Generate an image and persist it at the given storage location.
     *
     * @param target          routing / output header
     * @param prompt          text prompt
     * @param size            output size, nullable
     * @param steps           steps, nullable
     * @param seed            seed, nullable
     * @param guidanceScale   guidance scale, nullable
     * @param negativePrompt  negative prompt, nullable
     * @param imageUrl        input image url (image-to-image), nullable
     * @param imageBytes      input image bytes (image-to-image), nullable
     * @param location        storage instance + bucket locator (required)
     * @return persisted media reference, never null
     */
    public AiMediaRef generate(String operatorId, AiInvokeTarget target, String prompt, String size, Integer steps,
            Long seed, Double guidanceScale, String negativePrompt, String imageUrl, byte[] imageBytes,
            AiMediaLocation location) {
        authorizationPort.assertHas(operatorId, AiImagePermissionCodes011.GENERATE);

        if (target == null) {
            throw BusinessException.badRequest("target is required");
        }

        if (StringUtil011.isBlank(prompt)) {
            throw BusinessException.badRequest("prompt is required");
        }

        if (location == null) {
            throw BusinessException.badRequest("storage location is required");
        }

        AiModelProvider provider = resolver.resolveProvider(target.providerCode(), target.providerId());
        AiModelProviderApi api = resolver.resolveApi(provider, target.keyCode(), target.keyId());
        String model = resolver.requireModel(provider, target.model());

        AiImagePort port = registry.imagePort(provider.providerCode());

        if (port == null) {
            throw BusinessException.badRequest("no image provider for: " + provider.providerCode());
        }

        AiImageRequest request = new AiImageRequest(provider.baseUrl(), api.apiKey(),
                resolvedTarget(target, provider, model), prompt, size, steps, seed, guidanceScale, negativePrompt,
                imageUrl, imageBytes);

        AiMedia media = port.generate(request);

        return mediaStore.save(media, location);
    }

    /**
     * Replace the nullable routing refs with the resolved provider model so the
     * port sees a fully resolved target.
     *
     * @param target   original target
     * @param provider resolved provider
     * @param model    resolved model
     * @return resolved target
     */
    private AiInvokeTarget resolvedTarget(AiInvokeTarget target, AiModelProvider provider, String model) {
        return new AiInvokeTarget(provider.providerCode(), provider.id().value(), target.keyCode(), target.keyId(),
                model, target.returnType());
    }
}
