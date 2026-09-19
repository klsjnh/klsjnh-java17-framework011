package com.klsjnh.web.ai011.controller;

/*                AiImageController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai image controller class
 *
 */

import com.klsjnh.common.response.Response011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.ai011.image.AiImageUseCase;
import com.klsjnh.domain.ai011.capability.AiInvokeTarget;
import com.klsjnh.domain.ai011.capability.AiMedia;

import com.klsjnh.web.ai011.converter.AiMediaConverter;

import com.klsjnh.web.ai011.vo.aiimage.AiImageRequestVo011;
import com.klsjnh.web.ai011.vo.aiimage.AiImageResponseVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

/**
 * AI image HTTP adapter ({@code /klsjnh/ai011/julyAiImage/v1}): resolve the
 * provider / key by id or code and generate an image (text-to-image, or
 * image-to-image when an input image is given).
 */

@Tag(name = "AI模型接入011 - 文生图/图生图")
@RestController
@RequestMapping("/klsjnh/ai011/julyAiImage/v1")
public class AiImageController {

    /**
     * Image use case.
     */
    private final AiImageUseCase aiImageUseCase;

    /**
     * Media converter.
     */
    private final AiMediaConverter aiMediaConverter;

    /**
     * Create the controller.
     *
     * @param aiImageUseCase   image use case
     * @param aiMediaConverter media converter
     */
    public AiImageController(AiImageUseCase aiImageUseCase, AiMediaConverter aiMediaConverter) {
        this.aiImageUseCase = aiImageUseCase;
        this.aiMediaConverter = aiMediaConverter;
    }

    /**
     * Generate an image.
     *
     * @param vo request
     * @return image artifact in the requested form
     */
    @PostMapping("/generate")
    @Operation(summary = "文生图 / 图生图（provider/api 支持 id 或 code；有输入图即图生图）")
    public Response011<AiImageResponseVo011> generate(@RequestBody AiImageRequestVo011 vo) {
        String funcName = "ai image generate";

        AiInvokeTarget target = new AiInvokeTarget(vo.getProvider(), vo.getProviderId(), vo.getApi(), vo.getApiId(),
                vo.getModel(), vo.getReturnType());

        byte[] imageBytes = StringUtil011.isBlank(vo.getImageBase64()) ? null
                : Base64.getDecoder().decode(vo.getImageBase64());

        AiMedia media = aiImageUseCase.generate(target, vo.getPrompt(), vo.getSize(), vo.getSteps(), vo.getSeed(),
                vo.getGuidanceScale(), vo.getNegativePrompt(), vo.getImageUrl(), imageBytes);

        return Response011.success(funcName, aiMediaConverter.toImageVo(media));
    }
}
