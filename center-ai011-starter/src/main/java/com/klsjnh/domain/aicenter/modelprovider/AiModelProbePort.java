package com.klsjnh.domain.aicenter.modelprovider;

/*                AiModelProbePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model probe port interface
 *
 */

/**
 * Connectivity probe port for an OpenAI-compatible endpoint: a GET on
 * {@code {baseUrl}/models} with the api key (provider connectivity), plus an
 * optional low-cost chat probe that actually exercises
 * {@code POST /chat/completions} (model + auth + response shape). Implementation
 * lives in infrastructure; a probe never changes state and never logs the key.
 */

public interface AiModelProbePort {

    /**
     * Probe an endpoint's connectivity / auth with the given api key (GET
     * {@code /models}). A success here does NOT prove chat or vision works.
     *
     * @param baseUrl OpenAI-compatible base url
     * @param apiKey  api key secret
     * @return probe result, never null
     */
    ProbeResult probe(String baseUrl, String apiKey);

    /**
     * Probe one model over {@code POST /chat/completions} with a minimal prompt:
     * proves endpoint reachability, key validity, model availability and a
     * parseable reply shape. Implementations may keep the default (unsupported)
     * when the vendor has no chat endpoint.
     *
     * @param baseUrl OpenAI-compatible base url
     * @param apiKey  api key secret
     * @param model   model name
     * @return probe result, never null
     */
    default ProbeResult probeChat(String baseUrl, String apiKey, String model) {
        return new ProbeResult(false, "chat probe is not supported by this provider adapter", null);
    }

    /**
     * Probe outcome.
     *
     * @param success    true when the endpoint answered a 2xx
     * @param message    safe human readable detail (never contains the key)
     * @param httpStatus http status when a response was received, else null
     */
    record ProbeResult(boolean success, String message, Integer httpStatus) {
    }
}
