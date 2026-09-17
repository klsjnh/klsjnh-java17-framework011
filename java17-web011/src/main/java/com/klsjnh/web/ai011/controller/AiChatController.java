package com.klsjnh.web.ai011.controller;

/*                AiChatController class
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
 *
 */

import com.klsjnh.common.response.Response011;

import com.klsjnh.application.ai011.AiInvokeOutcome;
import com.klsjnh.application.ai011.AiInvokeUseCase;
import com.klsjnh.domain.ai011.AiChatMessage;

import com.klsjnh.web.ai011.vo.aichat.AiChatMessageVo011;
import com.klsjnh.web.ai011.vo.aichat.AiChatRequestVo011;
import com.klsjnh.web.ai011.vo.aichat.AiChatResponseVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * AI chat HTTP adapter ({@code /klsjnh/ai011/julyAiChat/v1}): resolve the
 * provider / key by id or code and return the model reply. The same capability
 * is available as a program entry via {@link AiInvokeUseCase}.
 */

@Tag(name = "AI模型接入011 - 调用")
@RestController
@RequestMapping("/klsjnh/ai011/julyAiChat/v1")
public class AiChatController {

    /**
     * AI invoke use case (the service-layer capability).
     */
    private final AiInvokeUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase ai invoke use case
     */
    public AiChatController(AiInvokeUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Chat with a model.
     *
     * @param vo request (provider / api / model / messages)
     * @return chat response (key masked)
     */
    @PostMapping("/chat")
    @Operation(summary = "AI 对话（provider/api 支持 id 或 code；缺省用默认密钥与首个模型）")
    public Response011<AiChatResponseVo011> chat(@RequestBody AiChatRequestVo011 vo) {
        String funcName = "ai chat";

        List<AiChatMessage> messages = new ArrayList<>();

        if (vo.getMessages() != null) {
            for (AiChatMessageVo011 message : vo.getMessages()) {
                messages.add(new AiChatMessage(message.getRole(), message.getContent()));
            }
        }

        AiInvokeOutcome outcome = useCase.chat(vo.getProvider(), vo.getApi(), vo.getModel(), messages,
                vo.getTemperature(), vo.getMaxTokens());

        return Response011.success(funcName, toVo(outcome));
    }

    /**
     * Map the outcome to the response VO.
     *
     * @param outcome invocation outcome
     * @return response VO
     */
    private AiChatResponseVo011 toVo(AiInvokeOutcome outcome) {
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
