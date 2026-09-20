package com.klsjnh.infrastructure.aicenter.prompt;

/*                PromptRender011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  prompt render (inline / object storage)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.aicenter.prompt.JulyAiPrompt;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptDetail;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptDetailRepository;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptRepository;
import com.klsjnh.domain.aicenter.prompt.PromptRenderPort;
import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.StorageResolverPort;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Prompt render implementation: resolve the prompt by code, load the domain
 * detail (inline text or an object from the storage center) and substitute
 * {@code ${var}} placeholders.
 */

@Component
public class PromptRender011 implements PromptRenderPort {

    /**
     * Prompt repository.
     */
    private final JulyAiPromptRepository promptRepository;

    /**
     * Detail repository.
     */
    private final JulyAiPromptDetailRepository detailRepository;

    /**
     * Storage resolver (for the storage content mode).
     */
    private final StorageResolverPort storageResolver;

    /**
     * Create the render.
     *
     * @param promptRepository prompt repository
     * @param detailRepository detail repository
     * @param storageResolver  storage resolver
     */
    public PromptRender011(JulyAiPromptRepository promptRepository, JulyAiPromptDetailRepository detailRepository,
            StorageResolverPort storageResolver) {
        this.promptRepository = promptRepository;
        this.detailRepository = detailRepository;
        this.storageResolver = storageResolver;
    }

    /** {@inheritDoc} */
    @Override
    public String render(String promptCode, String domainCode, Map<String, String> params) {
        JulyAiPrompt prompt = promptRepository.findByCode(promptCode);

        if (prompt == null) {
            throw BusinessException.recordNotFound("prompt: " + promptCode);
        }

        String pkMt = prompt.id().value();
        JulyAiPromptDetail detail = StringUtil011.isBlank(domainCode)
                ? first(detailRepository.findByMaster(pkMt))
                : detailRepository.findByDomain(pkMt, domainCode);

        if (detail == null) {
            throw BusinessException.badRequest("no prompt content for domain: " + domainCode);
        }

        return substitute(content(detail), params);
    }

    /**
     * Load the detail content (inline text or object storage).
     *
     * @param detail detail row
     * @return content text, nullable
     */
    private String content(JulyAiPromptDetail detail) {
        if (JulyAiPromptDetail.MODE_STORAGE.equalsIgnoreCase(detail.contentMode())) {
            ObjectStoragePort adapter = storageResolver.resolve(detail.storageCode());
            byte[] bytes = adapter.get(detail.bucket(), detail.objectKey());
            return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
        }

        return detail.content();
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

    /**
     * First element of a list, or null.
     *
     * @param rows rows
     * @return first row or null
     */
    private JulyAiPromptDetail first(List<JulyAiPromptDetail> rows) {
        return rows == null || rows.isEmpty() ? null : rows.get(0);
    }
}
