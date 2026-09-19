package com.klsjnh.application.ai011.chat;

/*                AiInvokeUseCase class
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
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.ai011.chat.AiChatCommand;
import com.klsjnh.domain.ai011.chat.AiChatMessage;
import com.klsjnh.domain.ai011.chat.AiChatPort;
import com.klsjnh.domain.ai011.chat.AiChatResult;
import com.klsjnh.domain.ai011.modelprovider.AiModelProvider;
import com.klsjnh.domain.ai011.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.ai011.modelprovider.AiModelProviderApiRepository;
import com.klsjnh.domain.ai011.modelprovider.AiModelProviderRepository;
import com.klsjnh.domain.iam.user.UserAuditPort;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI invocation service (program entry): resolves the provider and api key by
 * <b>id or code</b>, picks the model, calls the chat port, masks the key and
 * writes an audit row. Designed to be injected by other use cases (AI
 * self-development) as well as exposed over HTTP.
 */

@Service
public class AiInvokeUseCase {

    /**
     * Provider repository.
     */
    private final AiModelProviderRepository providerRepository;

    /**
     * Api (key) repository.
     */
    private final AiModelProviderApiRepository apiRepository;

    /**
     * Chat port.
     */
    private final AiChatPort chatPort;

    /**
     * User audit port.
     */
    private final UserAuditPort userAuditPort;

    /**
     * Create the use case.
     *
     * @param providerRepository provider repository
     * @param apiRepository      api repository
     * @param chatPort           chat port
     * @param userAuditPort      user audit port
     */
    public AiInvokeUseCase(AiModelProviderRepository providerRepository, AiModelProviderApiRepository apiRepository,
            AiChatPort chatPort, UserAuditPort userAuditPort) {
        this.providerRepository = providerRepository;
        this.apiRepository = apiRepository;
        this.chatPort = chatPort;
        this.userAuditPort = userAuditPort;
    }

    /**
     * Chat with a model, resolving the provider / key by id or code.
     *
     * @param providerRef provider id or providerCode
     * @param apiRef      api id or apiCode, blank for the default enabled key
     * @param model       model name, blank for the provider's first model
     * @param messages    chat messages
     * @param temperature sampling temperature, nullable
     * @param maxTokens   max output tokens, nullable
     * @return invocation outcome (key masked, usage included)
     */
    public AiInvokeOutcome chat(String providerRef, String apiRef, String model, List<AiChatMessage> messages,
            Double temperature, Integer maxTokens) {
        AiModelProvider provider = resolveProvider(providerRef);
        AiModelProviderApi api = resolveApi(provider, apiRef);
        String resolvedModel = resolveModel(provider, model);
        AiChatResult result = chatPort.chat(new AiChatCommand(provider.baseUrl(), api.apiKey(), resolvedModel,
                messages, temperature, maxTokens));

        audit(provider.providerCode(), api.apiCode(), resolvedModel, result);

        return new AiInvokeOutcome(provider.providerCode(), api.apiCode(), resolvedModel, result.content(),
                result.finishReason(), result.promptTokens(), result.completionTokens(), result.totalTokens());
    }

    /**
     * Convenience overload: default key and default model.
     *
     * @param providerRef provider id or providerCode
     * @param messages    chat messages
     * @return invocation outcome
     */
    public AiInvokeOutcome chat(String providerRef, List<AiChatMessage> messages) {
        return chat(providerRef, null, null, messages, null, null);
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
    private String resolveModel(AiModelProvider provider, String model) {
        if (model == null || model.isBlank()) {
            throw BusinessException.badRequest("model is required for provider: " + provider.providerCode());
        }

        return model.trim();
    }

    /**
     * Write an audit row (no key); never breaks the call.
     *
     * @param providerCode provider code
     * @param apiCode      api code
     * @param model        model name
     * @param result       chat result
     */
    private void audit(String providerCode, String apiCode, String model, AiChatResult result) {
        try {
            String content = "ai chat provider=" + providerCode + " api=" + apiCode + " model=" + model
                    + " tokens=" + result.totalTokens();
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
