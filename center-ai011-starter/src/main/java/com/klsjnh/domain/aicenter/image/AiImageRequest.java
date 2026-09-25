package com.klsjnh.domain.aicenter.image;

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
 *      2026.09.24  clarify text / image / text+image modes
 *
 */

import com.klsjnh.domain.aicenter.capability.AiInvokeTarget;

/**
 * One image generation request, fully resolved by the application layer:
 * endpoint + key + model in {@code target}, the parameters beside it. Modes
 * (vendor-neutral; each adapter maps to its wire API):
 * <ul>
 * <li><b>text-to-image</b> — prompt only (no {@code imageUrl} / {@code imageBytes})</li>
 * <li><b>image-to-image</b> / <b>text+image-to-image</b> — prompt + input image (url or
 * bytes); vendors that separate edit from generate (e.g. SenseNova) route this to edits</li>
 * </ul>
 *
 * @param baseUrl        provider base url (e.g. https://token.sensenova.cn/v1)
 * @param apiKey         api key secret, never echoed back
 * @param target         routing / output header (provider code / model / returnType)
 * @param prompt         text prompt, required (edit instruction when an input image is present)
 * @param size           output size (e.g. 1024x1024), nullable for provider default
 * @param steps          inference steps, nullable
 * @param seed           random seed, nullable
 * @param guidanceScale  classifier-free guidance scale, nullable
 * @param negativePrompt negative prompt, nullable
 * @param imageUrl       input image url (image / text+image), nullable
 * @param imageBytes     input image bytes (image / text+image), nullable
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
