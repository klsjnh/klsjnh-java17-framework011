package com.klsjnh.application.aicenter.inference;

/*                AiInferenceUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai invoke use case class
 *      2026.09.20  inference use case + streaming
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.aicenter.AiCapabilityRegistry;
import com.klsjnh.domain.aicenter.inference.AiChatMessage;
import com.klsjnh.domain.aicenter.inference.AiInferenceChunk;
import com.klsjnh.domain.aicenter.inference.AiInferenceCommand;
import com.klsjnh.domain.aicenter.inference.AiInferencePort;
import com.klsjnh.domain.aicenter.inference.AiInferenceResult;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApiRepository;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderRepository;
import com.klsjnh.domain.iam.user.UserAuditPort;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * AI inference use case (program entry): resolves the provider and api key by
 * <b>id or code</b>, requires an explicit model, dispatches to the inference
 * port and writes an audit row. Designed to be injected by other use cases (AI
 * self-development) as well as exposed over HTTP.
 */

@Service
public class AiInferenceUseCase {

    /**
     * Provider repository.
     */
    private final AiModelProviderRepository providerRepository;

    /**
     * Api (key) repository.
     */
    private final AiModelProviderApiRepository apiRepository;

    /**
     * Capability registry.
     */
    private final AiCapabilityRegistry registry;

    /**
     * User audit port.
     */
    private final UserAuditPort userAuditPort;

    /**
     * Create the use case.
     *
     * @param providerRepository provider repository
     * @param apiRepository      api repository
     * @param registry           capability registry
     * @param userAuditPort      user audit port
     */
    public AiInferenceUseCase(AiModelProviderRepository providerRepository, AiModelProviderApiRepository apiRepository,
            AiCapabilityRegistry registry, UserAuditPort userAuditPort) {
        this.providerRepository = providerRepository;
        this.apiRepository = apiRepository;
        this.registry = registry;
        this.userAuditPort = userAuditPort;
    }

    /**
     * Infer, resolving the provider / key by id or code.
     *
     * @param providerRef provider id or providerCode
     * @param apiRef      api id or apiCode, blank for the default enabled key
     * @param model       model name, required
     * @param messages    chat messages
     * @param temperature sampling temperature, nullable
     * @param maxTokens   max output tokens, nullable
     * @return inference outcome (key masked, usage included)
     */
    public AiInferenceOutcome chat(String providerRef, String apiRef, String model, List<AiChatMessage> messages,
            Double temperature, Integer maxTokens) {
        AiModelProvider provider = resolveProvider(providerRef);
        AiModelProviderApi api = resolveApi(provider, apiRef);
        String resolvedModel = requireModel(provider, model);
        AiInferencePort port = requireInferencePort(provider);
        AiInferenceResult result = port.chat(new AiInferenceCommand(provider.baseUrl(), api.apiKey(), resolvedModel,
                messages, temperature, maxTokens));

        audit(provider.providerCode(), api.apiCode(), resolvedModel, result);

        return new AiInferenceOutcome(provider.providerCode(), api.apiCode(), resolvedModel, result.content(),
                result.finishReason(), result.promptTokens(), result.completionTokens(), result.totalTokens());
    }

    /**
     * Convenience overload: default key, model still required.
     *
     * @param providerRef provider id or providerCode
     * @param model       model name, required
     * @param messages    chat messages
     * @return inference outcome
     */
    public AiInferenceOutcome chat(String providerRef, String model, List<AiChatMessage> messages) {
        return chat(providerRef, null, model, messages, null, null);
    }

    /**
     * Stream the inference fragments, resolving the provider / key by id or code.
     *
     * @param providerRef provider id or providerCode
     * @param apiRef      api id or apiCode, blank for the default enabled key
     * @param model       model name, required
     * @param messages    chat messages
     * @param temperature sampling temperature, nullable
     * @param maxTokens   max output tokens, nullable
     * @return fragment stream, never null
     */
    public Stream<AiInferenceChunk> stream(String providerRef, String apiRef, String model,
            List<AiChatMessage> messages, Double temperature, Integer maxTokens) {
        AiModelProvider provider = resolveProvider(providerRef);
        AiModelProviderApi api = resolveApi(provider, apiRef);
        String resolvedModel = requireModel(provider, model);
        AiInferencePort port = requireInferencePort(provider);

        audit(provider.providerCode(), api.apiCode(), resolvedModel, null);

        return port.stream(new AiInferenceCommand(provider.baseUrl(), api.apiKey(), resolvedModel, messages,
                temperature, maxTokens));
    }

    /**
     * Resolve the provider by id first, then by enabled code.
     *
     * @param ref provider id or code
     * @return provider
     */
    private AiModelProvider resolveProvider(String ref) {
        if (ref == null || ref.isBlank()) {
            throw BusinessException.badRequest("provider is required");
        }

        AiModelProvider provider = providerRepository.findById(ref);

        if (provider == null) {
            provider = providerRepository.findEnabledByCode(ref);
        }

        if (provider == null) {
            throw BusinessException.recordNotFound("ai provider: " + ref);
        }

        return provider;
    }

    /**
     * Resolve the api key by id first, then by code (within the provider);
     * blank falls back to the default enabled key.
     *
     * @param provider provider
     * @param ref      api id or code, nullable
     * @return api key entity
     */
    private AiModelProviderApi resolveApi(AiModelProvider provider, String ref) {
        String providerId = provider.id().value();

        if (ref == null || ref.isBlank()) {
            List<AiModelProviderApi> apis = apiRepository.findByMaster(providerId);
            if (apis.isEmpty()) {
                throw BusinessException.badRequest("no enabled api key for provider: " + provider.providerCode());
            }
            return apis.get(0);
        }

        AiModelProviderApi api = apiRepository.findById(ref);

        if (api == null) {
            api = apiRepository.findByMasterAndCode(providerId, ref);
        }

        if (api == null) {
            throw BusinessException.recordNotFound("ai api key: " + ref);
        }

        return api;
    }

    /**
     * Require an explicit model: the caller must name it (a provider serves
     * many models, so there is no safe default). Blank is a 400.
     *
     * @param provider provider
     * @param model    model name, required
     * @return trimmed model name
     */
    private String requireModel(AiModelProvider provider, String model) {
        if (model == null || model.isBlank()) {
            throw BusinessException.badRequest("model is required for provider: " + provider.providerCode());
        }

        return model.trim();
    }

    /**
     * Require an inference port for the provider.
     *
     * @param provider provider
     * @return inference port
     */
    private AiInferencePort requireInferencePort(AiModelProvider provider) {
        AiInferencePort port = registry.inferencePort(provider.providerCode());

        if (port == null) {
            throw BusinessException.badRequest("no inference provider for: " + provider.providerCode());
        }

        return port;
    }

    /**
     * Write an audit row (no key); never breaks the call.
     *
     * @param providerCode provider code
     * @param apiCode      api code
     * @param model        model name
     * @param result       inference result, nullable for streams
     */
    private void audit(String providerCode, String apiCode, String model, AiInferenceResult result) {
        try {
            String tokens = result == null ? "-" : String.valueOf(result.totalTokens());
            String content = "ai inference provider=" + providerCode + " api=" + apiCode + " model=" + model
                    + " tokens=" + tokens;
            userAuditPort.record(null, null, AuditType011.UPDATE, providerCode, content, null);
        } catch (Exception ignored) {
            // audit must never break the invocation
        }
    }

    /**
     * Build the message list from value pairs (service-friendly helper).
     *
     * @param role    role
     * @param content content
     * @return single-message list
     */
    public static List<AiChatMessage> one(String role, String content) {
        List<AiChatMessage> messages = new ArrayList<>();
        messages.add(new AiChatMessage(role, content));

        return messages;
    }
}
