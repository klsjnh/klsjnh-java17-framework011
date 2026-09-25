package com.klsjnh.web.aicenter.controller;

/*                AiAudioController class
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
 *      2026.09.20  audio module: tts + asr
 *
 */

import com.klsjnh.common.response.Response011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.aicenter.audio.AiAsrUseCase;
import com.klsjnh.application.aicenter.audio.AiTtsUseCase;
import com.klsjnh.domain.aicenter.audio.AiAsrResult;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.media.AiMediaLocation;
import com.klsjnh.domain.aicenter.media.AiMediaRef;

import com.klsjnh.web.aicenter.converter.AiMediaConverter;

import com.klsjnh.web.aicenter.vo.aiaudio.AiAsrRequestVo011;
import com.klsjnh.web.aicenter.vo.aiaudio.AiAsrResponseVo011;
import com.klsjnh.web.aicenter.vo.aiaudio.AiTtsRequestVo011;
import com.klsjnh.web.aicenter.vo.aiaudio.AiTtsResponseVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

/**
 * AI audio HTTP adapter ({@code /klsjnh/aicenter/julyAiAudio/v1}): speech
 * synthesis (tts) and speech recognition (asr), resolving the provider / key by
 * id or code.
 */

@Tag(name = "AI中心011 - 语音")
@RestController
@RequestMapping("/klsjnh/aicenter/julyAiAudio/v1")
public class AiAudioController {

    /**
     * Tts use case.
     */
    private final AiTtsUseCase aiTtsUseCase;

    /**
     * Asr use case.
     */
    private final AiAsrUseCase aiAsrUseCase;

    /**
     * Media converter.
     */
    private final AiMediaConverter aiMediaConverter;

    /**
     * Create the controller.
     *
     * @param aiTtsUseCase     tts use case
     * @param aiAsrUseCase     asr use case
     * @param aiMediaConverter media converter
     */
    public AiAudioController(AiTtsUseCase aiTtsUseCase, AiAsrUseCase aiAsrUseCase, AiMediaConverter aiMediaConverter) {
        this.aiTtsUseCase = aiTtsUseCase;
        this.aiAsrUseCase = aiAsrUseCase;
        this.aiMediaConverter = aiMediaConverter;
    }

    /**
     * Synthesize speech.
     *
     * @param vo request
     * @return persisted audio reference
     */
    @PostMapping("/synthesize")
    @Operation(summary = "文字转语音（必传 storageCode|storageId + bucketCode|bucketId；缺参 400）")
    public Response011<AiTtsResponseVo011> synthesize(@RequestBody AiTtsRequestVo011 vo) {
        String funcName = "ai tts synthesize";

        AiInvokeTarget target = new AiInvokeTarget(vo.getProvider(), vo.getProviderId(), vo.getApi(), vo.getApiId(),
                vo.getModel(), vo.getReturnType());

        AiMediaLocation location = new AiMediaLocation(vo.getStorageCode(), vo.getStorageId(), vo.getBucketCode(),
                vo.getBucketId());

        AiMediaRef ref = aiTtsUseCase.synthesize(target, vo.getInput(), vo.getVoice(), vo.getInstruction(),
                vo.getSpeed(), vo.getVolume(), vo.getFormat(), vo.getSampleRate(), location);

        return Response011.success(funcName, aiMediaConverter.toTtsVo(ref));
    }

    /**
     * Recognize speech.
     *
     * @param vo request
     * @return transcribed text
     */
    @PostMapping("/recognize")
    @Operation(summary = "语音识别（provider/api 支持 id 或 code；音频传 base64 或 url）")
    public Response011<AiAsrResponseVo011> recognize(@RequestBody AiAsrRequestVo011 vo) {
        String funcName = "ai asr recognize";

        AiInvokeTarget target = new AiInvokeTarget(vo.getProvider(), vo.getProviderId(), vo.getApi(), vo.getApiId(),
                vo.getModel(), null);

        byte[] audio = StringUtil011.isBlank(vo.getAudioBase64()) ? null
                : Base64.getDecoder().decode(vo.getAudioBase64());

        AiAsrResult result = aiAsrUseCase.recognize(target, audio, vo.getAudioUrl(), vo.getFormat(),
                vo.getLanguage());

        AiAsrResponseVo011 response = new AiAsrResponseVo011();
        response.setText(result.text());
        response.setFinishReason(result.finishReason());

        return Response011.success(funcName, response);
    }
}
