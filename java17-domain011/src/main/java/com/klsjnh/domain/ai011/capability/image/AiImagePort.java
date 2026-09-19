package com.klsjnh.domain.ai011.capability.image;

/*                AiImagePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai image capability SPI
 *
 */

import com.klsjnh.domain.ai011.capability.AiMedia;

/**
 * SPI: an image generation provider (text-to-image / image-to-image).
 * Implementations are collected by the AI provider registry (Spring
 * {@code List} injection), so a vendor is added by declaring one more bean; the
 * platform is not modified. Provider codes are an open string vocabulary.
 */

public interface AiImagePort {

    /**
     * Whether this provider serves the given provider code.
     *
     * @param providerCode provider code
     * @return true when supported
     */
    boolean supports(String providerCode);

    /**
     * Generate an image.
     *
     * @param request vendor-neutral request
     * @return generated media artifact, never null
     */
    AiMedia generate(AiImageRequest request);
}
