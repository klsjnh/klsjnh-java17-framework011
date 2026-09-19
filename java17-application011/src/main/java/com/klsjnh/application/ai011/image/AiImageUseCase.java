package com.klsjnh.application.ai011.image;

/*                AiImageUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai image use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.ai011.AiProviderRegistry;
import com.klsjnh.application.ai011.AiProviderResolver;
import com.klsjnh.domain.ai011.capability.AiInvokeTarget;
import com.klsjnh.domain.ai011.capability.AiMedia;
import com.klsjnh.domain.ai011.capability.image.AiImagePort;
import com.klsjnh.domain.ai011.capability.image.AiImageRequest;
import com.klsjnh.domain.ai011.modelprovider.AiModelProvider;
import com.klsjnh.domain.ai011.modelprovider.AiModelProviderApi;

import org.springframework.stereotype.Service;

/**
 * AI image generation use case: resolves the provider / key by id or code, then
 * dispatches to the capability port that supports the provider (text-to-image or
 * image-to-image, chosen by the presence of an input image).
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
    private final AiProviderRegistry registry;

    /**
     * Create the use case.
     *
     * @param resolver provider resolver
     * @param registry capability registry
     */
    public AiImageUseCase(AiProviderResolver resolver, AiProviderRegistry registry) {
        this.resolver = resolver;
        this.registry = registry;
    }

    /**
     * Generate an image.
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
     * @return generated media artifact, never null
     */
    public AiMedia generate(AiInvokeTarget target, String prompt, String size, Integer steps, Long seed,
            Double guidanceScale, String negativePrompt, String imageUrl, byte[] imageBytes) {
        if (target == null) {
            throw BusinessException.badRequest("target is required");
        }

        if (StringUtil011.isBlank(prompt)) {
            throw BusinessException.badRequest("prompt is required");
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

        return port.generate(request);
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
