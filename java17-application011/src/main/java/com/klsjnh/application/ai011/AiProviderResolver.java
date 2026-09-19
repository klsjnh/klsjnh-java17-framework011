package com.klsjnh.application.ai011;

/*                AiProviderResolver class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai provider resolver class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.ai011.modelprovider.AiModelProvider;
import com.klsjnh.domain.ai011.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.ai011.modelprovider.AiModelProviderApiRepository;
import com.klsjnh.domain.ai011.modelprovider.AiModelProviderRepository;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves an AI provider and its api key (by id OR code, blank falls back to
 * the default) for the capability use cases (chat / image / tts), and requires
 * an explicit model (a provider serves many models, so there is no safe
 * default).
 */

@Component
public class AiProviderResolver {

    /**
     * Provider repository.
     */
    private final AiModelProviderRepository providerRepository;

    /**
     * Api key repository.
     */
    private final AiModelProviderApiRepository apiRepository;

    /**
     * Create the resolver.
     *
     * @param providerRepository provider repository
     * @param apiRepository      api key repository
     */
    public AiProviderResolver(AiModelProviderRepository providerRepository, AiModelProviderApiRepository apiRepository) {
        this.providerRepository = providerRepository;
        this.apiRepository = apiRepository;
    }

    /**
     * Resolve the provider by id first, then by enabled code.
     *
     * @param providerCode provider code, nullable
     * @param providerId   provider id, nullable
     * @return provider
     */
    public AiModelProvider resolveProvider(String providerCode, String providerId) {
        AiModelProvider provider = null;

        if (providerId != null && !providerId.isBlank()) {
            provider = providerRepository.findById(providerId);
        }

        if (provider == null && providerCode != null && !providerCode.isBlank()) {
            provider = providerRepository.findByCode(providerCode);
        }

        if (provider == null) {
            String ref = providerId != null && !providerId.isBlank() ? providerId : providerCode;
            throw BusinessException.recordNotFound("ai provider: " + ref);
        }

        return provider;
    }

    /**
     * Resolve the api key by id first, then by code (within the provider);
     * blank falls back to the default enabled key.
     *
     * @param provider provider
     * @param keyCode  key code, nullable
     * @param keyId    key id, nullable
     * @return api key entity
     */
    public AiModelProviderApi resolveApi(AiModelProvider provider, String keyCode, String keyId) {
        String providerId = provider.id().value();

        if (keyId != null && !keyId.isBlank()) {
            AiModelProviderApi byId = apiRepository.findById(keyId);
            if (byId != null) {
                return byId;
            }
        }

        if (keyCode != null && !keyCode.isBlank()) {
            AiModelProviderApi byCode = apiRepository.findByMasterAndCode(providerId, keyCode);
            if (byCode != null) {
                return byCode;
            }
        }

        List<AiModelProviderApi> apis = apiRepository.findByMaster(providerId);
        if (apis.isEmpty()) {
            throw BusinessException.badRequest("no enabled api key for provider: " + provider.providerCode());
        }

        return apis.get(0);
    }

    /**
     * Require an explicit model: the caller must name it (chat / image / tts
     * each use different models under one provider, so there is no safe
     * default). Blank is a 400.
     *
     * @param provider provider (for the message)
     * @param model    model name, required
     * @return trimmed model name
     */
    public String requireModel(AiModelProvider provider, String model) {
        if (model == null || model.isBlank()) {
            throw BusinessException.badRequest("model is required for provider: " + provider.providerCode());
        }

        return model.trim();
    }
}
