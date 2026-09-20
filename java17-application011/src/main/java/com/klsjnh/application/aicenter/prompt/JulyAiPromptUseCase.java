package com.klsjnh.application.aicenter.prompt;

/*                JulyAiPromptUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt use case (crud + render)
 *      2026.09.20  storage content mode: write the body to object storage
 *      2026.09.20  prompt update + detail crud + getContent
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.aicenter.prompt.JulyAiPrompt;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptDetail;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptDetailRepository;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptQuerySpec;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptRepository;
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
 * AI prompt use case: prompt / detail CRUD, storage-mode body persistence and
 * rendering a prompt for AI development. A {@code storage} detail writes its
 * body to the storage center and keeps only a pointer; {@code inline} keeps the
 * text in the column.
 */

@Service
public class JulyAiPromptUseCase {

    /**
     * Default bucket for prompt bodies.
     */
    private static final String DEFAULT_BUCKET = "ai-prompt";

    /**
     * Prompt repository.
     */
    private final JulyAiPromptRepository promptRepository;

    /**
     * Detail repository.
     */
    private final JulyAiPromptDetailRepository detailRepository;

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
     * @param detailRepository detail repository
     * @param promptRender     prompt render port
     * @param storageResolver  storage resolver
     */
    public JulyAiPromptUseCase(JulyAiPromptRepository promptRepository, JulyAiPromptDetailRepository detailRepository,
            PromptRenderPort promptRender, StorageResolverPort storageResolver) {
        this.promptRepository = promptRepository;
        this.detailRepository = detailRepository;
        this.promptRender = promptRender;
        this.storageResolver = storageResolver;
    }

    /**
     * Insert a prompt with its domain details.
     *
     * @param promptCode prompt code
     * @param promptName prompt name
     * @param scene      scene
     * @param sortOrder  sort order
     * @param remark     remark
     * @param details    domain details
     * @return new prompt id
     */
    @Transactional
    public String insert(String promptCode, String promptName, String scene, Integer sortOrder, String remark,
            List<PromptDetailCommand> details) {
        String id = EntityId.generate().value();
        JulyAiPrompt prompt = JulyAiPrompt.create(EntityId.of(id), promptCode, promptName, scene, sortOrder, remark,
                AuditInfo.empty());
        promptRepository.insert(prompt);

        if (details != null && !details.isEmpty()) {
            int order = 1;
            for (PromptDetailCommand detail : details) {
                PromptDetailCommand normalized = detail.sortOrder() == null
                        ? new PromptDetailCommand(detail.domainCode(), detail.contentMode(), detail.content(),
                                detail.storageCode(), detail.bucket(), detail.variables(), order, detail.remark(),
                                detail.status())
                        : detail;
                insertDetail(id, normalized);
                order++;
            }
        }

        return id;
    }

    /**
     * Update a prompt (promptCode is immutable).
     *
     * @param id         prompt id
     * @param promptName prompt name
     * @param scene      scene
     * @param sortOrder  sort order
     * @param remark     remark
     * @param status     row status, null keeps the stored one
     * @return the prompt id
     */
    @Transactional
    public String update(String id, String promptName, String scene, Integer sortOrder, String remark, String status) {
        JulyAiPrompt prompt = requirePrompt(id);
        prompt.update(promptName, scene, sortOrder, remark, status);
        promptRepository.update(prompt);

        return id;
    }

    /**
     * Find a prompt by code.
     *
     * @param promptCode prompt code
     * @return prompt
     */
    public JulyAiPrompt getByCode(String promptCode) {
        JulyAiPrompt prompt = promptRepository.findByCode(promptCode);

        if (prompt == null) {
            throw BusinessException.recordNotFound("prompt: " + promptCode);
        }

        return prompt;
    }

    /**
     * Find a prompt by id.
     *
     * @param id prompt id
     * @return prompt
     */
    public JulyAiPrompt getById(String id) {
        return requirePrompt(id);
    }

    /**
     * The domain details of a prompt.
     *
     * @param pkMt prompt id
     * @return detail rows
     */
    public List<JulyAiPromptDetail> details(String pkMt) {
        return detailRepository.findByMaster(pkMt);
    }

    /**
     * Page query.
     *
     * @param query page query
     * @param spec  filter
     * @return page result
     */
    public PageResult011<JulyAiPrompt> selectListByPage(PageQuery011 query, JulyAiPromptQuerySpec spec) {
        long total = promptRepository.count(spec);
        List<JulyAiPrompt> rows = promptRepository.findPage(query.offset(), query.pageSize(), spec);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Logic delete a prompt and its details.
     *
     * @param id prompt id
     * @return the deleted id
     */
    @Transactional
    public String logicDelete(String id) {
        requirePrompt(id);
        detailRepository.logicDeleteByMaster(id);
        promptRepository.logicDeleteById(id);

        return id;
    }

    /**
     * Insert one detail under a prompt.
     *
     * @param pkMt    prompt id
     * @param command detail command
     * @return new detail id
     */
    @Transactional
    public String insertDetail(String pkMt, PromptDetailCommand command) {
        requirePrompt(pkMt);
        JulyAiPromptDetail row = JulyAiPromptDetail.create(EntityId.generate(), pkMt, command.domainCode(),
                command.contentMode(), command.content(), command.variables(), command.sortOrder(), null,
                AuditInfo.empty());

        if (isStorage(row)) {
            store(row, command);
        }

        detailRepository.insert(row);

        return row.id().value();
    }

    /**
     * Update one detail.
     *
     * @param id      detail id
     * @param command detail command
     * @return the detail id
     */
    @Transactional
    public String updateDetail(String id, PromptDetailCommand command) {
        JulyAiPromptDetail row = requireDetail(id);

        if (isStorage(row) || JulyAiPromptDetail.MODE_STORAGE.equalsIgnoreCase(command.contentMode())) {
            row.update(JulyAiPromptDetail.MODE_STORAGE, null, command.storageCode(), command.bucket(), null, null, null,
                    command.variables(), command.sortOrder(), command.remark(), command.status());
            store(row, command);
        } else {
            row.update(JulyAiPromptDetail.MODE_INLINE, command.content(), null, null, null, null, null,
                    command.variables(), command.sortOrder(), command.remark(), command.status());
        }

        detailRepository.update(row);

        return id;
    }

    /**
     * Logic delete one detail.
     *
     * @param id detail id
     * @return the detail id
     */
    @Transactional
    public String logicDeleteDetail(String id) {
        requireDetail(id);
        detailRepository.logicDeleteById(id);

        return id;
    }

    /**
     * Read a detail body (inline text or object storage).
     *
     * @param id detail id
     * @return content text, nullable
     */
    public String getContent(String id) {
        return readContent(requireDetail(id));
    }

    /**
     * Render a prompt by code.
     *
     * @param promptCode prompt code
     * @param domainCode business domain, blank for the default
     * @param params     variable values, nullable
     * @return rendered text
     */
    public String render(String promptCode, String domainCode, Map<String, String> params) {
        return promptRender.render(promptCode, domainCode, params);
    }

    /**
     * Require a prompt by id.
     *
     * @param id prompt id
     * @return prompt
     */
    private JulyAiPrompt requirePrompt(String id) {
        JulyAiPrompt prompt = promptRepository.findById(id);

        if (prompt == null) {
            throw BusinessException.recordNotFound("prompt: " + id);
        }

        return prompt;
    }

    /**
     * Require a detail by id.
     *
     * @param id detail id
     * @return detail
     */
    private JulyAiPromptDetail requireDetail(String id) {
        JulyAiPromptDetail detail = detailRepository.findById(id);

        if (detail == null) {
            throw BusinessException.recordNotFound("prompt detail: " + id);
        }

        return detail;
    }

    /**
     * Whether a detail uses the storage content mode.
     *
     * @param detail detail
     * @return true when storage mode
     */
    private boolean isStorage(JulyAiPromptDetail detail) {
        return JulyAiPromptDetail.MODE_STORAGE.equalsIgnoreCase(detail.contentMode());
    }

    /**
     * Write a storage-mode detail body to the storage center and point at it.
     *
     * @param row     detail aggregate (updated in place)
     * @param command detail command
     */
    private void store(JulyAiPromptDetail row, PromptDetailCommand command) {
        if (StringUtil011.isBlank(command.content())) {
            throw BusinessException.badRequest("storage content is required for domain: " + command.domainCode());
        }

        JulyAiPrompt prompt = promptRepository.findById(row.pkMt());
        String code = prompt == null ? row.pkMt() : prompt.promptCode();
        String bucket = StringUtil011.isBlank(command.bucket()) ? DEFAULT_BUCKET : command.bucket().trim();
        String key = "prompt/" + code + "/" + row.domainCode() + ".md";
        byte[] bytes = command.content().getBytes(StandardCharsets.UTF_8);
        ObjectStoragePort adapter = storageResolver.resolve(command.storageCode());

        if (!adapter.bucketExists(bucket)) {
            adapter.createBucket(bucket);
        }

        adapter.put(bucket, key, bytes, "text/markdown");
        row.update(JulyAiPromptDetail.MODE_STORAGE, null, command.storageCode(), bucket, key, sha256(bytes),
                (long) bytes.length, command.variables(), row.sortOrder(), row.remark(), row.status());
    }

    /**
     * Read a detail body.
     *
     * @param detail detail
     * @return content text, nullable
     */
    private String readContent(JulyAiPromptDetail detail) {
        if (!isStorage(detail)) {
            return detail.content();
        }

        ObjectStoragePort adapter = storageResolver.resolve(detail.storageCode());
        byte[] bytes = adapter.get(detail.bucket(), detail.objectKey());

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
