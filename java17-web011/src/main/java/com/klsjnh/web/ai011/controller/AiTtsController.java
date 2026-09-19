package com.klsjnh.web.ai011.controller;

/*                AiTtsController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai tts controller class
 *
 */

import com.klsjnh.common.response.Response011;

import com.klsjnh.application.ai011.tts.AiTtsUseCase;
import com.klsjnh.domain.ai011.capability.AiInvokeTarget;
import com.klsjnh.domain.ai011.capability.AiMedia;

import com.klsjnh.web.ai011.converter.AiMediaConverter;

import com.klsjnh.web.ai011.vo.aitts.AiTtsRequestVo011;
import com.klsjnh.web.ai011.vo.aitts.AiTtsResponseVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI speech synthesis HTTP adapter ({@code /klsjnh/ai011/julyAiTts/v1}):
 * resolve the provider / key by id or code and synthesize speech from text.
 */

@Tag(name = "AI模型接入011 - 文字转语音")
@RestController
@RequestMapping("/klsjnh/ai011/julyAiTts/v1")
public class AiTtsController {

    /**
     * Tts use case.
     */
    private final AiTtsUseCase aiTtsUseCase;

    /**
     * Media converter.
     */
    private final AiMediaConverter aiMediaConverter;

    /**
     * Create the controller.
     *
     * @param aiTtsUseCase     tts use case
     * @param aiMediaConverter media converter
     */
    public AiTtsController(AiTtsUseCase aiTtsUseCase, AiMediaConverter aiMediaConverter) {
        this.aiTtsUseCase = aiTtsUseCase;
        this.aiMediaConverter = aiMediaConverter;
    }

    /**
     * Synthesize speech.
     *
     * @param vo request
     * @return audio artifact in the requested form
     */
    @PostMapping("/synthesize")
    @Operation(summary = "文字转语音（provider/api 支持 id 或 code）")
    public Response011<AiTtsResponseVo011> synthesize(@RequestBody AiTtsRequestVo011 vo) {
        String funcName = "ai tts synthesize";

        AiInvokeTarget target = new AiInvokeTarget(vo.getProvider(), vo.getProviderId(), vo.getApi(), vo.getApiId(),
                vo.getModel(), vo.getReturnType());

        AiMedia media = aiTtsUseCase.synthesize(target, vo.getInput(), vo.getVoice(), vo.getInstruction(),
                vo.getSpeed(), vo.getVolume(), vo.getFormat(), vo.getSampleRate());

        return Response011.success(funcName, aiMediaConverter.toTtsVo(media));
    }
}
