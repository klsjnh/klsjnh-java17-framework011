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
 * {@code {baseUrl}/models} with the api key. Implementation lives in
 * infrastructure; the probe never changes state.
 */

public interface AiModelProbePort {

    /**
     * Probe an endpoint with the given api key.
     *
     * @param baseUrl OpenAI-compatible base url
     * @param apiKey  api key secret
     * @return probe result, never null
     */
    ProbeResult probe(String baseUrl, String apiKey);

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
