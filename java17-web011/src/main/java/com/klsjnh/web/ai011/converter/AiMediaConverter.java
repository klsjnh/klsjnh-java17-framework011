package com.klsjnh.web.ai011.converter;

/*                AiMediaConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai media converter class
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.ai011.capability.AiMedia;
import com.klsjnh.domain.ai011.capability.AiReturnTypes;

import com.klsjnh.web.ai011.vo.aiimage.AiImageResponseVo011;
import com.klsjnh.web.ai011.vo.aitts.AiTtsResponseVo011;

import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * Converter between the shared {@link AiMedia} artifact and the image / tts
 * response VOs. Bytes are exposed as base64.
 */

@Component
public class AiMediaConverter {

    /**
     * Map an artifact to the image response VO.
     *
     * @param media artifact
     * @return image response VO
     */
    public AiImageResponseVo011 toImageVo(AiMedia media) {
        AiImageResponseVo011 vo = new AiImageResponseVo011();
        vo.setReturnType(media.returnType());
        vo.setUrl(media.url());
        vo.setBase64(base64(media));
        vo.setMimeType(media.mimeType());

        return vo;
    }

    /**
     * Map an artifact to the tts response VO.
     *
     * @param media artifact
     * @return tts response VO
     */
    public AiTtsResponseVo011 toTtsVo(AiMedia media) {
        AiTtsResponseVo011 vo = new AiTtsResponseVo011();
        vo.setReturnType(media.returnType());
        vo.setUrl(media.url());
        vo.setBase64(base64(media));
        vo.setMimeType(media.mimeType());

        return vo;
    }

    /**
     * The base64 form of an artifact: the base64 field when present, otherwise
     * the encoding of the raw bytes.
     *
     * @param media artifact
     * @return base64 string or null
     */
    private String base64(AiMedia media) {
        if (!StringUtil011.isBlank(media.base64())) {
            return media.base64();
        }

        if (media.bytes() != null && media.bytes().length > 0) {
            return Base64.getEncoder().encodeToString(media.bytes());
        }

        if (AiReturnTypes.BYTES.equalsIgnoreCase(media.returnType())) {
            return "";
        }

        return null;
    }
}
