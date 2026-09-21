package com.klsjnh.application.aicenter.prompt;

/*                JulyAiDomainPromptUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain prompt use case (crud + render)
 *      2026.09.21  storage content mode: body to object storage, prompt/<code>.md
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.aicenter.prompt.JulyAiDomain;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPrompt;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPromptQuerySpec;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPromptRepository;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainRepository;
import com.klsjnh.domain.aicenter.prompt.PromptRenderPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.StorageResolverPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

/**
 * JulyAiDomainPrompt use cases: prompt CRUD under a domain, storage-mode body
 * persistence and rendering a prompt for AI development. A {@code storage}
 * prompt writes its body to the storage center and keeps only a pointer;
 * {@code inline} keeps the text in the column.
 */

@Service
public class JulyAiDomainPromptUseCase {

    /**
     * Default bucket for prompt bodies.
     */
    private static final String DEFAULT_BUCKET = "ai-prompt";

    /**
     * Prompt repository.
     */
    private final JulyAiDomainPromptRepository promptRepository;

    /**
     * Domain repository (master validation).
     */
    private final JulyAiDomainRepository domainRepository;

    /**
     * Prompt render port.
     */
    private final PromptRenderPort promptRender;

    /**
     * Storage resolver (for the storage content mode).
     */
    private final StorageResolverPort storageResolver;

    /**
     * Create the use case.
     *
     * @param promptRepository prompt repository
     * @param domainRepository domain repository
     * @param promptRender     prompt render port
     * @param storageResolver  storage resolver
     */
    public JulyAiDomainPromptUseCase(JulyAiDomainPromptRepository promptRepository,
            JulyAiDomainRepository domainRepository, PromptRenderPort promptRender,
            StorageResolverPort storageResolver) {
        this.promptRepository = promptRepository;
        this.domainRepository = domainRepository;
        this.promptRender = promptRender;
        this.storageResolver = storageResolver;
    }

    /**
     * Insert a prompt under an enabled domain.
     *
     * @param pkMt        domain id
     * @param promptCode  prompt code
     * @param promptName  prompt name
     * @param scene       scene
     * @param contentMode content mode
     * @param content     inline content
     * @param storageCode storage instance code
     * @param bucket      bucket
     * @param variables   variable declarations
     * @param sortOrder   sort order
     * @param remark      remark
     * @param status      row status, null falls back to enabled
     * @return new prompt id
     */
    @Transactional
    public String insert(String pkMt, String promptCode, String promptName, String scene, String contentMode,
            String content, String storageCode, String bucket, String variables, Integer sortOrder, String remark,
            String status) {
        requireEnabledDomain(pkMt);

        JulyAiDomainPrompt row = JulyAiDomainPrompt.create(EntityId.generate(), pkMt, promptCode, promptName, scene,
                contentMode, content, variables, sortOrder, remark, status, AuditInfo.empty());

        if (isStorage(row)) {
            store(row, content, storageCode, bucket);
        }

        promptRepository.insert(row);

        return row.id().value();
    }

    /**
     * Update a prompt (promptCode is immutable).
     *
     * @param id          prompt id
     * @param promptName  prompt name
     * @param scene       scene
     * @param contentMode content mode
     * @param content     inline content
     * @param storageCode storage instance code
     * @param bucket      bucket
     * @param variables   variable declarations
     * @param sortOrder   sort order
     * @param remark      remark
     * @param status      row status, null keeps the stored one
     * @return the prompt id
     */
    @Transactional
    public String update(String id, String promptName, String scene, String contentMode, String content,
            String storageCode, String bucket, String variables, Integer sortOrder, String remark, String status) {
        JulyAiDomainPrompt row = require(id);

        if (JulyAiDomainPrompt.MODE_STORAGE.equalsIgnoreCase(contentMode)) {
            row.update(promptName, scene, JulyAiDomainPrompt.MODE_STORAGE, null, storageCode, bucket, null, null, null,
                    variables, sortOrder, remark, status);
            store(row, content, storageCode, bucket);
        } else {
            row.update(promptName, scene, JulyAiDomainPrompt.MODE_INLINE, content, null, null, null, null, null,
                    variables, sortOrder, remark, status);
        }

        promptRepository.update(row);

        return id;
    }

    /**
     * Find a prompt by id.
     *
     * @param id prompt id
     * @return prompt
     */
    public JulyAiDomainPrompt getById(String id) {
        return require(id);
    }

    /**
     * Find a prompt by code.
     *
     * @param promptCode prompt code
     * @return prompt
     */
    public JulyAiDomainPrompt getByCode(String promptCode) {
        JulyAiDomainPrompt prompt = promptRepository.findByCode(promptCode);

        if (prompt == null) {
            throw BusinessException.recordNotFound("prompt: " + promptCode);
        }

        return prompt;
    }

    /**
     * Read a prompt body (inline text or object storage).
     *
     * @param id prompt id
     * @return content text, nullable
     */
    public String getContent(String id) {
        return readContent(require(id));
    }

    /**
     * Page query.
     *
     * @param query page query
     * @param spec  filter
     * @return page result
     */
    public PageResult011<JulyAiDomainPrompt> selectListByPage(PageQuery011 query, JulyAiDomainPromptQuerySpec spec) {
        PageQuery011 pageQuery = query == null ? new PageQuery011(1, 10) : query;
        long total = promptRepository.count(spec);
        List<JulyAiDomainPrompt> rows = promptRepository.findPage(pageQuery.offset(), pageQuery.pageSize(), spec);

        return PageResult011.of(pageQuery, total, rows);
    }

    /**
     * Logic delete a prompt.
     *
     * @param id prompt id
     * @return the deleted id
     */
    @Transactional
    public String logicDelete(String id) {
        require(id);
        promptRepository.logicDeleteById(id);

        return id;
    }

    /**
     * Render a prompt by code.
     *
     * @param promptCode prompt code, globally unique
     * @param params     variable values, nullable
     * @return rendered text
     */
    public String render(String promptCode, Map<String, String> params) {
        return promptRender.render(promptCode, params);
    }

    /**
     * Require a prompt by id.
     *
     * @param id prompt id
     * @return prompt
     */
    private JulyAiDomainPrompt require(String id) {
        JulyAiDomainPrompt prompt = promptRepository.findById(id);

        if (prompt == null) {
            throw BusinessException.recordNotFound("prompt: " + id);
        }

        return prompt;
    }

    /**
     * Require an existing and enabled domain.
     *
     * @param pkMt domain id
     * @return enabled domain
     */
    private JulyAiDomain requireEnabledDomain(String pkMt) {
        JulyAiDomain domain = domainRepository.findById(pkMt);

        if (domain == null) {
            throw BusinessException.badRequest("domain not found, id=" + pkMt);
        }

        if (!Status011.ENABLED.getCode().equals(domain.status())) {
            throw BusinessException.badRequest("domain is disabled, id=" + pkMt);
        }

        return domain;
    }

    /**
     * Whether a prompt uses the storage content mode.
     *
     * @param prompt prompt
     * @return true when storage mode
     */
    private boolean isStorage(JulyAiDomainPrompt prompt) {
        return JulyAiDomainPrompt.MODE_STORAGE.equalsIgnoreCase(prompt.contentMode());
    }

    /**
     * Write a storage-mode prompt body to the storage center and point at it.
     *
     * @param row         prompt aggregate (updated in place)
     * @param content     inline content to persist
     * @param storageCode storage instance code
     * @param bucket      bucket
     */
    private void store(JulyAiDomainPrompt row, String content, String storageCode, String bucket) {
        if (StringUtil011.isBlank(content)) {
            throw BusinessException.badRequest("storage content is required for prompt: " + row.promptCode());
        }

        String bucketName = StringUtil011.isBlank(bucket) ? DEFAULT_BUCKET : bucket.trim();
        String key = "prompt/" + row.promptCode() + ".md";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        ObjectStoragePort adapter = storageResolver.resolve(storageCode);

        if (!adapter.bucketExists(bucketName)) {
            adapter.createBucket(bucketName);
        }

        adapter.put(bucketName, key, bytes, "text/markdown");
        row.update(row.promptName(), row.scene(), JulyAiDomainPrompt.MODE_STORAGE, null, storageCode, bucketName, key,
                sha256(bytes), (long) bytes.length, row.variables(), row.sortOrder(), row.remark(), row.status());
    }

    /**
     * Read a prompt body.
     *
     * @param prompt prompt
     * @return content text, nullable
     */
    private String readContent(JulyAiDomainPrompt prompt) {
        if (!isStorage(prompt)) {
            return prompt.content();
        }

        ObjectStoragePort adapter = storageResolver.resolve(prompt.storageCode());
        byte[] bytes = adapter.get(prompt.bucket(), prompt.objectKey());

        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * SHA-256 hex of the content.
     *
     * @param bytes content bytes
     * @return hex digest
     */
    private String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder hex = new StringBuilder();

            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("sha-256 unavailable: " + ex.getMessage());
        }
    }
}
