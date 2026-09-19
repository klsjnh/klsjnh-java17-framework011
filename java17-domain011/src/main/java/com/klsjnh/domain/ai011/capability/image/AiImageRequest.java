package com.klsjnh.domain.ai011.capability.image;

/*                AiImageRequest record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai image request record
 *
 */

import com.klsjnh.domain.ai011.capability.AiInvokeTarget;

/**
 * One image generation request, fully resolved by the application layer:
 * endpoint + key + model in {@code target}, the parameters beside it. Text to
 * image when no input image is given, image to image when {@code imageUrl} or
 * {@code imageBytes} is present. Vendor-neutral; a provider maps these to its
 * own wire fields.
 *
 * @param baseUrl        provider base url (e.g. https://api.stepfun.com/v1)
 * @param apiKey         api key secret, never echoed back
 * @param target         routing / output header (provider code / model / returnType)
 * @param prompt         text prompt, required
 * @param size           output size (e.g. 1024x1024), nullable for provider default
 * @param steps          inference steps, nullable
 * @param seed           random seed, nullable
 * @param guidanceScale  classifier-free guidance scale, nullable
 * @param negativePrompt negative prompt, nullable
 * @param imageUrl       input image url (image-to-image), nullable
 * @param imageBytes     input image bytes (image-to-image), nullable
 */

public record AiImageRequest(String baseUrl, String apiKey, AiInvokeTarget target, String prompt, String size,
        Integer steps, Long seed, Double guidanceScale, String negativePrompt, String imageUrl, byte[] imageBytes) {

    /**
     * Whether this is an image-to-image request.
     *
     * @return true when an input image is present
     */
    public boolean isImageToImage() {
        return (imageUrl != null && !imageUrl.isBlank()) || (imageBytes != null && imageBytes.length > 0);
    }
}
