package com.klsjnh.application.aicenter.inference;

/*                AiInferenceUseCaseTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  option gating + image budget tests
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.aicenter.AiCapabilityRegistry;
import com.klsjnh.application.aicenter.AiProviderResolver;
import com.klsjnh.application.aicenter.ratelimit.AiRateLimiterPort;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.inference.AiChatContentPart;
import com.klsjnh.domain.aicenter.inference.AiChatMessage;
import com.klsjnh.domain.aicenter.inference.AiInferenceCommand;
import com.klsjnh.domain.aicenter.inference.AiInferenceOptions;
import com.klsjnh.domain.aicenter.inference.AiInferencePort;
import com.klsjnh.domain.aicenter.inference.AiInferenceResult;
import com.klsjnh.domain.aicenter.inference.AiReasoningEffort;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.iam.user.UserAuditPort;

import com.klsjnh.infrastructure.aicenter.config.KrtAiConfig011;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

/**
 * Unit tests for {@link AiInferenceUseCase}: structured-output / streaming
 * exclusivity, the reasoning allow-list and the image byte budget.
 */

class AiInferenceUseCaseTest {

    /**
     * Provider resolver.
     */
    private AiProviderResolver resolver;

    /**
     * Capability registry.
     */
    private AiCapabilityRegistry registry;

    /**
     * Inference port.
     */
    private AiInferencePort port;

    /**
     * Config.
     */
    private KrtAiConfig011 config;

    /**
     * Use case under test.
     */
    private AiInferenceUseCase useCase;

    /**
     * Routing header.
     */
    private AiInvokeTarget target;

    @BeforeEach
    void setUp() {
        resolver = mock(AiProviderResolver.class);
        registry = mock(AiCapabilityRegistry.class);
        port = mock(AiInferencePort.class);
        config = new KrtAiConfig011();

        AiModelProvider provider = mock(AiModelProvider.class);
        when(provider.providerCode()).thenReturn("p1");
        when(provider.baseUrl()).thenReturn("https://api.test/v1");

        AiModelProviderApi api = mock(AiModelProviderApi.class);
        when(api.apiKey()).thenReturn("sk-secret");
        when(api.apiCode()).thenReturn("a1");

        when(resolver.resolveProvider(any(), any())).thenReturn(provider);
        when(resolver.resolveApi(any(), any(), any())).thenReturn(api);
        when(resolver.requireModel(any(), any())).thenReturn("m1");
        when(registry.inferencePort("p1")).thenReturn(port);
        when(port.chat(any())).thenReturn(new AiInferenceResult("ok", "stop", 1, 1, 2));

        AiRateLimiterPort rateLimiter = mock(AiRateLimiterPort.class);
        when(rateLimiter.acquire(any(), any(), any())).thenReturn(AiRateLimiterPort.noop());
        useCase = new AiInferenceUseCase(resolver, registry, mock(UserAuditPort.class), mock(AuthorizationPort.class),
                config, rateLimiter);

        target = new AiInvokeTarget("p1", null, null, null, "m1", null);
    }

    @Test
    /**
     * Rejects JSON output combined with streaming.
     */
    void rejectsJsonOutputForStreaming() {
        AiInferenceOptions options = new AiInferenceOptions(true, null);

        assertThrows(BusinessException.class,
                () -> useCase.stream("u1", target, AiInferenceUseCase.one("user", "hi"), 0.1, 64, options));
    }

    @Test
    /**
     * Rejects reasoning for a provider outside the allow-list.
     */
    void rejectsReasoningWhenProviderIsNotAllowListed() {
        AiInferenceOptions options = new AiInferenceOptions(false, AiReasoningEffort.NONE);

        assertThrows(BusinessException.class,
                () -> useCase.chat("u1", target, AiInferenceUseCase.one("user", "hi"), 0.1, 64, options));
    }

    @Test
    /**
     * Forwards reasoning for an allow-listed provider, case insensitive.
     */
    void forwardsReasoningForAllowListedProvider() {
        config.getInference().setReasoningEffortProviders(List.of("P1"));

        useCase.chat("u1", target, AiInferenceUseCase.one("user", "hi"), 0.1, 64,
                new AiInferenceOptions(false, AiReasoningEffort.LOW));

        ArgumentCaptor<AiInferenceCommand> captor = ArgumentCaptor.forClass(AiInferenceCommand.class);
        verify(port).chat(captor.capture());
        assertEquals(AiReasoningEffort.LOW, captor.getValue().options().reasoningEffort());
    }

    @Test
    /**
     * Keeps the text-only call without options.
     */
    void keepsTextOnlyCallWithoutOptions() {
        useCase.chat("u1", target, AiInferenceUseCase.one("user", "hi"), 0.1, 64);

        ArgumentCaptor<AiInferenceCommand> captor = ArgumentCaptor.forClass(AiInferenceCommand.class);
        verify(port).chat(captor.capture());
        assertNull(captor.getValue().options());
        assertEquals("hi", captor.getValue().messages().get(0).content());
    }

    @Test
    /**
     * Rejects an image data URL over the byte budget.
     */
    void rejectsOversizedImageDataUrl() {
        config.getInference().setMaxImageDataUrlBytes(4);
        List<AiChatContentPart> parts = List.of(AiChatContentPart.text("look"),
                AiChatContentPart.imageUrl("data:image/png;base64,iVBORw0KGgo="));

        assertThrows(BusinessException.class,
                () -> useCase.chat("u1", target, AiInferenceUseCase.one("user", parts), 0.1, 64));
    }

    @Test
    /**
     * Accepts a multimodal message within the budget.
     */
    void acceptsMultimodalMessageWithinBudget() {
        List<AiChatContentPart> parts = List.of(AiChatContentPart.text("look"),
                AiChatContentPart.imageUrl("https://example.com/a.png"));

        useCase.chat("u1", target, AiInferenceUseCase.one("user", parts), 0.1, 64);

        ArgumentCaptor<AiInferenceCommand> captor = ArgumentCaptor.forClass(AiInferenceCommand.class);
        verify(port).chat(captor.capture());
        assertEquals(2, captor.getValue().messages().get(0).contentParts().size());
    }
}
