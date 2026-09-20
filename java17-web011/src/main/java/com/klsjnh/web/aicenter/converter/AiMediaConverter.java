package com.klsjnh.web.aicenter.converter;

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
 *      2026.09.20  map persisted media reference
 *
 */

import com.klsjnh.domain.aicenter.media.AiMediaRef;

import com.klsjnh.web.aicenter.vo.aiaudio.AiTtsResponseVo011;
import com.klsjnh.web.aicenter.vo.aiimage.AiImageResponseVo011;

import org.springframework.stereotype.Component;

/**
 * Converter between the persisted media reference and the image / tts response
 * VOs. The artifact is delivered as a url plus its storage key; the consumer
 * owns the lifecycle.
 */

@Component
public class AiMediaConverter {

    /**
     * Map a stored reference to the image response VO.
     *
     * @param ref stored media reference
     * @return image response VO
     */
    public AiImageResponseVo011 toImageVo(AiMediaRef ref) {
        AiImageResponseVo011 vo = new AiImageResponseVo011();
        vo.setReturnType("url");
        vo.setUrl(ref.url());
        vo.setStorageKey(ref.storageKey());
        vo.setMimeType(ref.mimeType());

        return vo;
    }

    /**
     * Map a stored reference to the tts response VO.
     *
     * @param ref stored media reference
     * @return tts response VO
     */
    public AiTtsResponseVo011 toTtsVo(AiMediaRef ref) {
        AiTtsResponseVo011 vo = new AiTtsResponseVo011();
        vo.setReturnType("url");
        vo.setUrl(ref.url());
        vo.setStorageKey(ref.storageKey());
        vo.setMimeType(ref.mimeType());

        return vo;
    }
}
