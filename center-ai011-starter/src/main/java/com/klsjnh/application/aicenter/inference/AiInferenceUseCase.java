package com.klsjnh.application.aicenter.inference;

/*                AiInferenceUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai invoke use case class
 *      2026.09.20  inference use case + streaming
 *      2026.09.20  resolve by provider code/id + key code/id (AiInvokeTarget)
 *      2026.09.26  explicit permission checks (aiInference)
 *      2026.10.22  multimodal messages + options (json output / reasoning effort)
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.aicenter.AiCapabilityRegistry;
import com.klsjnh.application.aicenter.AiProviderResolver;
import com.klsjnh.application.aicenter.ratelimit.AiRateLimiterPort;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.inference.AiChatContentPart;
import com.klsjnh.domain.aicenter.inference.AiChatMessage;
import com.klsjnh.domain.aicenter.inference.AiInferenceChunk;
import com.klsjnh.domain.aicenter.inference.AiInferenceCommand;
import com.klsjnh.domain.aicenter.inference.AiInferenceOptions;
import com.klsjnh.domain.aicenter.inference.AiInferencePermissionCodes011;
import com.klsjnh.domain.aicenter.inference.AiInferencePort;
import com.klsjnh.domain.aicenter.inference.AiInferenceResult;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.user.UserAuditPort;

import com.klsjnh.infrastructure.aicenter.config.KrtAiConfig011;

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
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * AI-center config (input guards + reasoning allow-list).
     */
    private final KrtAiConfig011 config;

    /**
     * Provider rate / concurrency guard.
     */
    private final AiRateLimiterPort rateLimiter;

    /**
     * Create the use case.
     *
     * @param resolver           provider resolver
     * @param registry           capability registry
     * @param userAuditPort      user audit port
     * @param authorizationPort  authorization port
     * @param config             ai-center config
     * @param rateLimiter        provider rate limiter
     */
    public AiInferenceUseCase(AiProviderResolver resolver, AiCapabilityRegistry registry, UserAuditPort userAuditPort,
            AuthorizationPort authorizationPort, KrtAiConfig011 config, AiRateLimiterPort rateLimiter) {
        this.resolver = resolver;
        this.registry = registry;
        this.userAuditPort = userAuditPort;
        this.authorizationPort = authorizationPort;
        this.config = config;
        this.rateLimiter = rateLimiter;
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
    public AiInferenceOutcome chat(String operatorId, AiInvokeTarget target, List<AiChatMessage> messages,
            Double temperature, Integer maxTokens) {
        return chat(operatorId, target, messages, temperature, maxTokens, null);
    }

    /**
     * Infer with optional structured-output / reasoning controls.
     *
     * @param target      routing header (provider code/id + key code/id + model)
     * @param messages    chat messages
     * @param temperature sampling temperature, nullable
     * @param maxTokens   max output tokens, nullable
     * @param options     optional controls, null for defaults
     * @return inference outcome (key masked, usage included)
     */
    public AiInferenceOutcome chat(String operatorId, AiInvokeTarget target, List<AiChatMessage> messages,
            Double temperature, Integer maxTokens, AiInferenceOptions options) {
        authorizationPort.assertHas(operatorId, AiInferencePermissionCodes011.CHAT);

        AiModelProvider provider = resolver.resolveProvider(target.providerCode(), target.providerId());
        AiModelProviderApi api = resolver.resolveApi(provider, target.keyCode(), target.keyId());
        String model = resolver.requireModel(provider, target.model());
        AiInferencePort port = requireInferencePort(provider);
        AiInferenceOptions resolved = resolveOptions(provider, options, false);
        validateContent(messages);
        AiInferenceResult result;

        try (AiRateLimiterPort.Permit ignored = rateLimiter.acquire(provider.providerCode(), api.apiCode(), model)) {
            result = port.chat(new AiInferenceCommand(provider.baseUrl(), api.apiKey(), model, messages,
                    temperature, maxTokens, resolved));
        }

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
    public Stream<AiInferenceChunk> stream(String operatorId, AiInvokeTarget target, List<AiChatMessage> messages,
            Double temperature, Integer maxTokens) {
        return stream(operatorId, target, messages, temperature, maxTokens, null);
    }

    /**
     * Stream with optional controls. Structured JSON output is rejected because
     * most OpenAI-compatible vendors do not support
     * {@code stream + response_format} together.
     *
     * @param target      routing header (provider code/id + key code/id + model)
     * @param messages    chat messages
     * @param temperature sampling temperature, nullable
     * @param maxTokens   max output tokens, nullable
     * @param options     optional controls, null for defaults
     * @return fragment stream, never null
     */
    public Stream<AiInferenceChunk> stream(String operatorId, AiInvokeTarget target, List<AiChatMessage> messages,
            Double temperature, Integer maxTokens, AiInferenceOptions options) {
        authorizationPort.assertHas(operatorId, AiInferencePermissionCodes011.CHAT);

        AiModelProvider provider = resolver.resolveProvider(target.providerCode(), target.providerId());
        AiModelProviderApi api = resolver.resolveApi(provider, target.keyCode(), target.keyId());
        String model = resolver.requireModel(provider, target.model());
        AiInferencePort port = requireInferencePort(provider);
        AiInferenceOptions resolved = resolveOptions(provider, options, true);
        validateContent(messages);

        audit(provider.providerCode(), api.apiCode(), model, null);
        AiRateLimiterPort.Permit permit = rateLimiter.acquire(provider.providerCode(), api.apiCode(), model);

        try {
            return port.stream(new AiInferenceCommand(provider.baseUrl(), api.apiKey(), model, messages, temperature,
                    maxTokens, resolved)).onClose(permit::close);
        } catch (RuntimeException ex) {
            permit.close();
            throw ex;
        }
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
     * Validate and gate the optional controls: structured output is rejected for
     * streaming, and the reasoning extension is only forwarded to allow-listed
     * providers.
     *
     * @param provider provider
     * @param options  requested options, nullable
     * @param stream   whether the call is a stream
     * @return options to send, null for defaults
     */
    private AiInferenceOptions resolveOptions(AiModelProvider provider, AiInferenceOptions options, boolean stream) {
        if (options == null) {
            return null;
        }

        if (stream && options.jsonObject()) {
            throw BusinessException.badRequest("json output is not supported with streaming");
        }

        if (options.reasoningEffort() != null && !supportsReasoning(provider.providerCode())) {
            throw BusinessException.badRequest("reasoning effort is not enabled for provider: "
                    + provider.providerCode());
        }

        return options;
    }

    /**
     * Whether the provider is allow-listed for {@code reasoning_effort}
     * ({@code krt.ai-center.inference.reasoning-effort-providers}).
     *
     * @param providerCode provider code
     * @return true when allow-listed
     */
    private boolean supportsReasoning(String providerCode) {
        for (String code : config.getInference().getReasoningEffortProviders()) {
            if (code != null && code.trim().equalsIgnoreCase(providerCode)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validate every image part against the runtime policy (scheme, base64,
     * byte budget). Text-only messages pass through untouched.
     *
     * @param messages chat messages
     */
    private void validateContent(List<AiChatMessage> messages) {
        long maxBytes = config.getInference().getMaxImageDataUrlBytes();

        try {
            for (AiChatMessage message : messages) {
                if (message.contentParts() == null) {
                    continue;
                }

                for (AiChatContentPart part : message.contentParts()) {
                    if (AiChatContentPart.TYPE_IMAGE_URL.equals(part.type())) {
                        AiChatContentPart.validateImageUrl(part.imageUrl(), maxBytes);
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
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

    /**
     * Build the message list from typed parts (service-friendly helper).
     *
     * @param role  role
     * @param parts content parts
     * @return single-message list
     */
    public static List<AiChatMessage> one(String role, List<AiChatContentPart> parts) {
        List<AiChatMessage> messages = new ArrayList<>();
        messages.add(new AiChatMessage(role, null, parts));

        return messages;
    }
}
