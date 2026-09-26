package com.klsjnh.infrastructure.aicenter.prompt;

/*                PromptRender011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate 2026.09.21
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  prompt render (inline / object storage)
 *      2026.09.21  resolve by globally unique prompt code only
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPrompt;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPromptRepository;
import com.klsjnh.domain.aicenter.prompt.PromptRenderPort;
import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.StorageResolverPort;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Prompt render implementation: resolve the prompt by its globally unique code,
 * load the content (inline text or an object from the storage center) and
 * substitute {@code ${var}} placeholders.
 */

@Component
public class PromptRender011 implements PromptRenderPort {

    /**
     * Prompt repository.
     */
    private final JulyAiDomainPromptRepository promptRepository;

    /**
     * Storage resolver (for the storage content mode).
     */
    private final StorageResolverPort storageResolver;

    /**
     * Create the render.
     *
     * @param promptRepository prompt repository
     * @param storageResolver  storage resolver
     */
    public PromptRender011(JulyAiDomainPromptRepository promptRepository, StorageResolverPort storageResolver) {
        this.promptRepository = promptRepository;
        this.storageResolver = storageResolver;
    }

    /** {@inheritDoc} */
    @Override
    public String render(String promptCode, Map<String, String> params) {
        JulyAiDomainPrompt prompt = promptRepository.findByCode(promptCode);

        if (prompt == null) {
            throw BusinessException.recordNotFound("prompt: " + promptCode);
        }

        return substitute(content(prompt), params);
    }

    /**
     * Load the prompt content (inline text or object storage).
     *
     * @param prompt prompt row
     * @return content text, nullable
     */
    private String content(JulyAiDomainPrompt prompt) {
        if (JulyAiDomainPrompt.MODE_STORAGE.equalsIgnoreCase(prompt.contentMode())) {
            ObjectStoragePort adapter = storageResolver.resolve(prompt.storageCode());
            byte[] bytes = adapter.get(prompt.bucket(), prompt.objectKey());
            return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
        }

        return prompt.content();
    }

    /**
     * Substitute {@code ${var}} placeholders.
     *
     * @param template template text
     * @param params   variable values
     * @return rendered text
     */
    private String substitute(String template, Map<String, String> params) {
        if (template == null || params == null || params.isEmpty()) {
            return template;
        }

        String result = template;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }

        return result;
    }
}
