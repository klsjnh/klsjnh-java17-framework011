package com.klsjnh.infrastructure.aicenter.modelprovider.probe;

/*                AiModelProbe011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model probe 011 class
 *
 */

import com.klsjnh.common.util.HttpResponse011;
import com.klsjnh.common.util.HttpUtil011;

import com.klsjnh.domain.aicenter.modelprovider.AiModelProbePort;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Connectivity probe over the shared JDK HTTP helper: GET {@code {baseUrl}/models}
 * with {@code Authorization: Bearer <apiKey>}. Only the status line is read;
 * the key never appears in any message.
 */

@Component
public class AiModelProbe011 implements AiModelProbePort {

    /**
     * Probe an endpoint with the given api key.
     *
     * @param baseUrl OpenAI-compatible base url
     * @param apiKey  api key secret
     * @return probe result, never null
     */
    @Override
    public ProbeResult probe(String baseUrl, String apiKey) {
        String url = normalize(baseUrl) + "/models";

        try {
            HttpResponse011 response = HttpUtil011.get(url,
                    Map.of("Authorization", "Bearer " + (apiKey == null ? "" : apiKey)));
            int status = response.status();

            return new ProbeResult(response.isSuccess(), "HTTP " + status, status);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            return new ProbeResult(false, "probe interrupted", null);
        } catch (Exception ex) {
            return new ProbeResult(false, ex.getClass().getSimpleName() + ": " + ex.getMessage(), null);
        }
    }

    /**
     * Strip trailing slashes from the base url.
     *
     * @param baseUrl base url
     * @return normalized base url
     */
    private String normalize(String baseUrl) {
        String value = baseUrl == null ? "" : baseUrl.trim();

        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }
}
