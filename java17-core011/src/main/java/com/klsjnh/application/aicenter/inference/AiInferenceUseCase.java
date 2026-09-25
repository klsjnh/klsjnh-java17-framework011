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
 *      2026.09.20  resolve by provider code/id + key code/id (AiInvokeTarget)
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.aicenter.AiCapabilityRegistry;
import com.klsjnh.application.aicenter.AiProviderResolver;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.inference.AiChatMessage;
import com.klsjnh.domain.aicenter.inference.AiInferenceChunk;
import com.klsjnh.domain.aicenter.inference.AiInferenceCommand;
import com.klsjnh.domain.aicenter.inference.AiInferencePort;
import com.klsjnh.domain.aicenter.inference.AiInferenceResult;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.iam.user.UserAuditPort;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * AI inference use case (program entry): resolves the provider by code/id and
 * the api key by code/id, requires an explicit model, dispatches to the
 * inference port and writes an audit row. Same routing header
 * ({@link AiInvokeTarget}) as the image / tts / asr capabilities. Designed to be
 * injected by other use cases (AI self-development) as well as exposed over HTTP.
 */

@Service
public class AiInferenceUseCase {

    /**
     * Provider / key resolver.
     */
    private final AiProviderResolver resolver;

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
     * @param resolver      provider resolver
     * @param registry      capability registry
     * @param userAuditPort user audit port
     */
    public AiInferenceUseCase(AiProviderResolver resolver, AiCapabilityRegistry registry, UserAuditPort userAuditPort) {
        this.resolver = resolver;
        this.registry = registry;
        this.userAuditPort = userAuditPort;
    }

    /**
     * Infer, resolving the provider and key by code/id.
     *
     * @param target      routing header (provider code/id + key code/id + model)
     * @param messages    chat messages
     * @param temperature sampling temperature, nullable
     * @param maxTokens   max output tokens, nullable
     * @return inference outcome (key masked, usage included)
     */
    public AiInferenceOutcome chat(AiInvokeTarget target, List<AiChatMessage> messages, Double temperature,
            Integer maxTokens) {
        AiModelProvider provider = resolver.resolveProvider(target.providerCode(), target.providerId());
        AiModelProviderApi api = resolver.resolveApi(provider, target.keyCode(), target.keyId());
        String model = resolver.requireModel(provider, target.model());
        AiInferencePort port = requireInferencePort(provider);
        AiInferenceResult result = port.chat(new AiInferenceCommand(provider.baseUrl(), api.apiKey(), model, messages,
                temperature, maxTokens));

        audit(provider.providerCode(), api.apiCode(), model, result);

        return new AiInferenceOutcome(provider.providerCode(), api.apiCode(), model, result.content(),
                result.finishReason(), result.promptTokens(), result.completionTokens(), result.totalTokens());
    }

    /**
     * Stream the inference fragments, resolving the provider and key by code/id.
     *
     * @param target      routing header (provider code/id + key code/id + model)
     * @param messages    chat messages
     * @param temperature sampling temperature, nullable
     * @param maxTokens   max output tokens, nullable
     * @return fragment stream, never null
     */
    public Stream<AiInferenceChunk> stream(AiInvokeTarget target, List<AiChatMessage> messages, Double temperature,
            Integer maxTokens) {
        AiModelProvider provider = resolver.resolveProvider(target.providerCode(), target.providerId());
        AiModelProviderApi api = resolver.resolveApi(provider, target.keyCode(), target.keyId());
        String model = resolver.requireModel(provider, target.model());
        AiInferencePort port = requireInferencePort(provider);

        audit(provider.providerCode(), api.apiCode(), model, null);

        return port.stream(new AiInferenceCommand(provider.baseUrl(), api.apiKey(), model, messages, temperature,
                maxTokens));
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
