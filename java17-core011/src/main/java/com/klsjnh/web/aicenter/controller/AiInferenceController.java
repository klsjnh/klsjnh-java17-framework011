package com.klsjnh.web.aicenter.controller;

/*                AiInferenceController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai chat controller class
 *      2026.09.20  inference module + sse streaming
 *
 */

import com.klsjnh.common.response.Response011;

import com.klsjnh.application.aicenter.inference.AiInferenceOutcome;
import com.klsjnh.application.aicenter.inference.AiInferenceUseCase;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.inference.AiChatMessage;
import com.klsjnh.domain.aicenter.inference.AiInferenceChunk;

import com.klsjnh.web.aicenter.vo.aichat.AiChatMessageVo011;
import com.klsjnh.web.aicenter.vo.aichat.AiChatRequestVo011;
import com.klsjnh.web.aicenter.vo.aichat.AiChatResponseVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * AI inference HTTP adapter ({@code /klsjnh/aicenter/julyAiInference/v1}):
 * resolve the provider / key by id or code and return the model reply, whole or
 * streamed over SSE. The same capability is available as a program entry via
 * {@link AiInferenceUseCase}.
 */

@ConditionalOnClass(name = "com.klsjnh.infrastructure.aicenter.media.AiMediaStore011")
@Tag(name = "AI中心011 - 推理")
@RestController
@RequestMapping("/klsjnh/aicenter/julyAiInference/v1")
public class AiInferenceController {

    /**
     * AI inference use case (the service-layer capability).
     */
    private final AiInferenceUseCase aiInferenceUseCase;

    /**
     * Create the controller.
     *
     * @param aiInferenceUseCase ai inference use case
     */
    public AiInferenceController(AiInferenceUseCase aiInferenceUseCase) {
        this.aiInferenceUseCase = aiInferenceUseCase;
    }

    /**
     * Infer and return the whole reply.
     *
     * @param vo request (provider / api / model / messages)
     * @return inference response (key masked)
     */
    @PostMapping("/chat")
    @Operation(summary = "AI 推理（provider/api 支持 id 或 code；model 必传）")
    public Response011<AiChatResponseVo011> chat(@RequestBody AiChatRequestVo011 vo) {
        String funcName = "ai inference";

        AiInvokeTarget target = new AiInvokeTarget(vo.getProvider(), vo.getProviderId(), vo.getApi(), vo.getApiId(),
                vo.getModel(), null);

        AiInferenceOutcome outcome = aiInferenceUseCase.chat(target, messages(vo), vo.getTemperature(),
                vo.getMaxTokens());

        return Response011.success(funcName, toVo(outcome));
    }

    /**
     * Infer and stream the reply over SSE.
     *
     * @param vo request (provider / api / model / messages)
     * @return sse emitter
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 推理流式（SSE；provider/api 支持 id 或 code；model 必传）")
    public SseEmitter chatStream(@RequestBody AiChatRequestVo011 vo) {
        AiInvokeTarget target = new AiInvokeTarget(vo.getProvider(), vo.getProviderId(), vo.getApi(), vo.getApiId(),
                vo.getModel(), null);

        Stream<AiInferenceChunk> stream = aiInferenceUseCase.stream(target, messages(vo), vo.getTemperature(),
                vo.getMaxTokens());

        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> emit(emitter, stream));

        return emitter;
    }

    /**
     * Build the domain message list from the request VO.
     *
     * @param vo request
     * @return messages
     */
    private List<AiChatMessage> messages(AiChatRequestVo011 vo) {
        List<AiChatMessage> messages = new ArrayList<>();

        if (vo.getMessages() != null) {
            for (AiChatMessageVo011 message : vo.getMessages()) {
                messages.add(new AiChatMessage(message.getRole(), message.getContent()));
            }
        }

        return messages;
    }

    /**
     * Emit the stream to the sse emitter.
     *
     * @param emitter sse emitter
     * @param stream  chunk stream
     */
    private void emit(SseEmitter emitter, Stream<AiInferenceChunk> stream) {
        try (stream) {
            stream.forEach(chunk -> send(emitter, chunk));
            emitter.send(SseEmitter.event().data("[DONE]", MediaType.TEXT_PLAIN));
            emitter.complete();
        } catch (Exception ex) {
            emitter.completeWithError(ex);
        }
    }

    /**
     * Send one chunk as an OpenAI-compatible SSE data frame
     * ({@code {"choices":[{"delta":{"content":"..."},"finish_reason":null}]}}),
     * so embedded newlines survive the transport (JSON-escaped) and the de-facto
     * standard client parser ({@code choices[0].delta.content}) works unchanged.
     *
     * @param emitter sse emitter
     * @param chunk   chunk
     */
    private void send(SseEmitter emitter, AiInferenceChunk chunk) {
        Map<String, Object> delta = new LinkedHashMap<>();

        if (chunk.content() != null) {
            delta.put("content", chunk.content());
        }

        Map<String, Object> choice = new LinkedHashMap<>();
        choice.put("delta", delta);
        choice.put("finish_reason", chunk.finishReason());

        Map<String, Object> frame = new LinkedHashMap<>();
        frame.put("choices", List.of(choice));

        try {
            emitter.send(SseEmitter.event().data(frame, MediaType.APPLICATION_JSON));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Map the outcome to the response VO.
     *
     * @param outcome inference outcome
     * @return response VO
     */
    private AiChatResponseVo011 toVo(AiInferenceOutcome outcome) {
        AiChatResponseVo011 vo = new AiChatResponseVo011();
        vo.setProviderCode(outcome.providerCode());
        vo.setApiCode(outcome.apiCode());
        vo.setModel(outcome.model());
        vo.setContent(outcome.content());
        vo.setFinishReason(outcome.finishReason());
        vo.setPromptTokens(outcome.promptTokens());
        vo.setCompletionTokens(outcome.completionTokens());
        vo.setTotalTokens(outcome.totalTokens());

        return vo;
    }
}
