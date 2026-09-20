package com.klsjnh.domain.aicenter.capability;

/*                AiInvokeTarget record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  ai invoke target record
 *
 */

/**
 * The routing / output header shared by every AI capability request: which
 * provider and key to use (by id OR code) and how the artifact comes back.
 * Built by the application layer after id/code resolution.
 *
 * @param providerCode provider code (alternative to providerId), nullable
 * @param providerId   provider id (alternative to providerCode), nullable
 * @param keyCode      api key code (alternative to keyId), nullable for default
 * @param keyId        api key id (alternative to keyCode), nullable for default
 * @param model        model name, nullable for the provider default
 * @param returnType   artifact form: URL / BYTES / B64, nullable for the
 *                     capability default (open string, see {@link AiReturnTypes})
 */

public record AiInvokeTarget(String providerCode, String providerId, String keyCode, String keyId, String model,
        String returnType) {
}
