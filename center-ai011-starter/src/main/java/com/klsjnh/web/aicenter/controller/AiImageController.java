package com.klsjnh.web.aicenter.controller;

/*                AiImageController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai image controller class
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.aicenter.image.AiImageUseCase;
import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;
import com.klsjnh.domain.aicenter.media.AiMediaLocation;
import com.klsjnh.domain.aicenter.media.AiMediaRef;

import com.klsjnh.web.aicenter.converter.AiMediaConverter;

import com.klsjnh.web.aicenter.vo.aiimage.AiImageRequestVo011;
import com.klsjnh.web.aicenter.vo.aiimage.AiImageResponseVo011;
import com.klsjnh.web.util.Operator011Resolver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Base64;

/**
 * AI image HTTP adapter ({@code /klsjnh/aicenter/julyAiImage/v1}): resolve the
 * provider / key by id or code and generate an image (text-to-image, or
 * image-to-image when an input image is given).
 */

@Tag(name = "AI中心011 - 文生图/图生图/文图生图")
@RestController
@RequestMapping("/klsjnh/aicenter/julyAiImage/v1")
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
    @Operation(summary = "文生图 / 图生图 / 文图生图（必传 storageCode|storageId + bucketCode|bucketId；缺参 400；有输入图即图生图/文图生图）")
    public Response011<AiImageResponseVo011> generate(@Valid @RequestBody AiImageRequestVo011 vo, HttpServletRequest request) {
        String funcName = "ai image generate";
        Operator011 operator = Operator011Resolver.resolve(request);


        AiInvokeTarget target = new AiInvokeTarget(vo.getProvider(), vo.getProviderId(), vo.getApi(), vo.getApiId(),
                vo.getModel(), vo.getReturnType());

        byte[] imageBytes = StringUtil011.isBlank(vo.getImageBase64()) ? null
                : Base64.getDecoder().decode(vo.getImageBase64());

        AiMediaLocation location = new AiMediaLocation(vo.getStorageCode(), vo.getStorageId(), vo.getBucketCode(),
                vo.getBucketId());

        AiMediaRef ref = aiImageUseCase.generate(operator.id(), target, vo.getPrompt(), vo.getSize(), vo.getSteps(), vo.getSeed(),
                vo.getGuidanceScale(), vo.getNegativePrompt(), vo.getImageUrl(), imageBytes, location);

        return Response011.success(funcName, aiMediaConverter.toImageVo(ref));
    }
}
