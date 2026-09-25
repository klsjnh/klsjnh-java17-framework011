package com.klsjnh.application.aicenter.prompt;

/*                JulyAiDomainUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain use case (tree crud)
 *      2026.09.21  merged with the prompt use case: one master-sub use case
 *                  (master save / whole save / child save + render)
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.aicenter.prompt.JulyAiDomain;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainBundle;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPrompt;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPromptQuerySpec;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPromptRepository;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainQuerySpec;
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
 * Master-sub use case for the prompt aggregate: master = business domain tree
 * (july_ai_domain), child = prompt (july_ai_domain_prompt).
 * <p>
 * Three save granularities, matching {@code BaseMasterSubRepository}:
 * master only (insert / update), whole (saveWhole: master + children in one
 * transaction, children replaced) and child only (insertDetail / updateDetail).
 * A {@code storage} prompt writes its body to the storage center and keeps only
 * a pointer; {@code inline} keeps the text in the column.
 * </p>
 */

@Service
public class JulyAiDomainUseCase {

    /**
     * Default bucket for prompt bodies.
     */
    private static final String DEFAULT_BUCKET = "ai-prompt";

    /**
     * Domain repository (master).
     */
    private final JulyAiDomainRepository repository;

    /**
     * Prompt repository (child).
     */
    private final JulyAiDomainPromptRepository promptRepository;

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
     * @param repository       domain repository
     * @param promptRepository prompt repository
     * @param promptRender     prompt render port
     * @param storageResolver  storage resolver
     */
    public JulyAiDomainUseCase(JulyAiDomainRepository repository, JulyAiDomainPromptRepository promptRepository,
            PromptRenderPort promptRender, StorageResolverPort storageResolver) {
        this.repository = repository;
        this.promptRepository = promptRepository;
        this.promptRender = promptRender;
        this.storageResolver = storageResolver;
    }

    /**
     * Insert a new domain node (master save).
     *
     * @param domainCode domain code, unique
     * @param domainName domain name
     * @param parentId   parent domain id, blank for root
     * @param sortOrder  sort order
     * @param remark     remark
     * @return new domain id
     */
    @Transactional
    public String insert(String domainCode, String domainName, String parentId, Integer sortOrder, String remark) {
        if (repository.findByCode(domainCode) != null) {
            throw BusinessException.badRequest("domain code already exists: " + domainCode);
        }

        String targetParent = parentId == null ? "" : parentId;
        requireParent(targetParent);

        JulyAiDomain domain = JulyAiDomain.create(EntityId.generate(), domainCode, domainName, targetParent, sortOrder,
                remark, AuditInfo.empty());
        repository.insert(domain);

        return domain.id().value();
    }

    /**
     * Update a domain (master save; domainCode is immutable).
     *
     * @param id         domain id
     * @param domainName domain name
     * @param parentId   parent domain id, blank for root
     * @param sortOrder  sort order
     * @param remark     remark
     * @param status     row status, null keeps the stored one
     * @return domain id
     */
    @Transactional
    public String update(String id, String domainName, String parentId, Integer sortOrder, String remark,
            String status) {
        JulyAiDomain domain = require(id);
        String targetParent = parentId == null ? "" : parentId;

        if (id.equals(targetParent)) {
            throw BusinessException.badRequest("domain parent cannot be itself");
        }

        requireParent(targetParent);
        domain.update(domainName, targetParent, sortOrder, remark, status);
        repository.update(domain);

        return domain.id().value();
    }

    /**
     * Find by primary key.
     *
     * @param id domain id
     * @return domain
     */
    public JulyAiDomain getById(String id) {
        return require(id);
    }

    /**
     * Find by domain code.
     *
     * @param domainCode domain code
     * @return domain
     */
    public JulyAiDomain getByCode(String domainCode) {
        JulyAiDomain domain = repository.findByCode(domainCode);

        if (domain == null) {
            throw BusinessException.recordNotFound("domain: " + domainCode);
        }

        return domain;
    }

    /**
     * Page query.
     *
     * @param query page query
     * @param spec  filter
     * @return page result
     */
    public PageResult011<JulyAiDomain> selectListByPage(PageQuery011 query, JulyAiDomainQuerySpec spec) {
        PageQuery011 pageQuery = query == null ? new PageQuery011(1, 10) : query;
        long total = repository.count(spec);
        List<JulyAiDomain> rows = repository.findPage(pageQuery.offset(), pageQuery.pageSize(), spec);

        return PageResult011.of(pageQuery, total, rows);
    }

    /**
     * The enabled domain tree (children nested, ordered by sort_order / id).
     * Tree assembly lives in the persistence base, not here.
     *
     * @return root nodes with nested children
     */
    public List<JulyAiDomain> selectTree() {
        return repository.findTree();
    }

    /**
     * Logic delete a domain; alive children and prompts both reject.
     *
     * @param id domain id
     * @return deleted domain id
     */
    @Transactional
    public String logicDelete(String id) {
        require(id);

        if (repository.hasChildren(id)) {
            throw BusinessException.badRequest("domain has children, delete children first");
        }

        if (promptRepository.countByMaster(id, false) > 0) {
            throw BusinessException.badRequest("domain has prompts, delete prompts first");
        }

        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Whole save (master + children, one transaction): the master is inserted
     * when {@code id} is blank and updated otherwise, then the old children are
     * logically deleted and the given list is inserted (replace strategy).
     *
     * @param id         domain id, blank inserts a new domain
     * @param domainCode domain code, used on insert only
     * @param domainName domain name
     * @param parentId   parent domain id, blank for root
     * @param sortOrder  sort order
     * @param remark     remark
     * @param status     row status, null keeps the stored one
     * @param prompts    prompt rows to replace, nullable for none
     * @return domain id
     */
    @Transactional
    public String saveWhole(String id, String domainCode, String domainName, String parentId, Integer sortOrder,
            String remark, String status, List<PromptSaveCommand> prompts) {
        String masterId = StringUtil011.isBlank(id)
                ? insert(domainCode, domainName, parentId, sortOrder, remark)
                : update(id, domainName, parentId, sortOrder, remark, status);

        promptRepository.logicDeleteByMaster(masterId);

        if (prompts == null || prompts.isEmpty()) {
            return masterId;
        }

        int order = 1;

        for (PromptSaveCommand prompt : prompts) {
            PromptSaveCommand normalized = prompt.sortOrder() == null
                    ? new PromptSaveCommand(prompt.promptCode(), prompt.promptName(), prompt.scene(),
                            prompt.contentMode(), prompt.content(), prompt.storageCode(), prompt.bucket(),
                            prompt.variables(), order, prompt.remark(), prompt.status())
                    : prompt;
            insertDetail(masterId, normalized);
            order++;
        }

        return masterId;
    }

    /**
     * Read a domain together with its prompts (master + children read).
     *
     * @param id domain id
     * @return domain with its ordered prompts
     */
    public JulyAiDomainBundle getWithChildren(String id) {
        return new JulyAiDomainBundle(require(id), promptRepository.findByMaster(id));
    }

    /**
     * Insert one prompt under an enabled domain (child save).
     *
     * @param pkMt    domain id
     * @param command prompt save command
     * @return new prompt id
     */
    @Transactional
    public String insertDetail(String pkMt, PromptSaveCommand command) {
        requireEnabledDomain(pkMt);

        JulyAiDomainPrompt row = JulyAiDomainPrompt.create(EntityId.generate(), pkMt, command.promptCode(),
                command.promptName(), command.scene(), command.contentMode(), command.content(), command.variables(),
                command.sortOrder(), command.remark(), command.status(), AuditInfo.empty());

        if (isStorage(row)) {
            store(row, command.content(), command.storageCode(), command.bucket());
        }

        promptRepository.insert(row);

        return row.id().value();
    }

    /**
     * Update one prompt (child save; promptCode is immutable).
     *
     * @param id      prompt id
     * @param command prompt save command
     * @return the prompt id
     */
    @Transactional
    public String updateDetail(String id, PromptSaveCommand command) {
        JulyAiDomainPrompt row = requirePrompt(id);

        if (JulyAiDomainPrompt.MODE_STORAGE.equalsIgnoreCase(command.contentMode())) {
            row.update(command.promptName(), command.scene(), JulyAiDomainPrompt.MODE_STORAGE, null,
                    command.storageCode(), command.bucket(), null, null, null, command.variables(), command.sortOrder(),
                    command.remark(), command.status());
            store(row, command.content(), command.storageCode(), command.bucket());
        } else {
            row.update(command.promptName(), command.scene(), JulyAiDomainPrompt.MODE_INLINE, command.content(), null,
                    null, null, null, null, command.variables(), command.sortOrder(), command.remark(),
                    command.status());
        }

        promptRepository.update(row);

        return id;
    }

    /**
     * Logic delete one prompt (child save).
     *
     * @param id prompt id
     * @return the deleted id
     */
    @Transactional
    public String logicDeleteDetail(String id) {
        requirePrompt(id);
        promptRepository.logicDeleteById(id);

        return id;
    }

    /**
     * Find a prompt by id.
     *
     * @param id prompt id
     * @return prompt
     */
    public JulyAiDomainPrompt getDetailById(String id) {
        return requirePrompt(id);
    }

    /**
     * Find a prompt by code.
     *
     * @param promptCode prompt code, globally unique
     * @return prompt
     */
    public JulyAiDomainPrompt getDetailByCode(String promptCode) {
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
        return readContent(requirePrompt(id));
    }

    /**
     * Page query the prompts (children).
     *
     * @param query page query
     * @param spec  filter
     * @return page result
     */
    public PageResult011<JulyAiDomainPrompt> selectDetailListByPage(PageQuery011 query,
            JulyAiDomainPromptQuerySpec spec) {
        PageQuery011 pageQuery = query == null ? new PageQuery011(1, 10) : query;
        long total = promptRepository.count(spec);
        List<JulyAiDomainPrompt> rows = promptRepository.findPage(pageQuery.offset(), pageQuery.pageSize(), spec);

        return PageResult011.of(pageQuery, total, rows);
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
     * Require an existing domain.
     *
     * @param id domain id
     * @return domain
     */
    private JulyAiDomain require(String id) {
        JulyAiDomain domain = repository.findById(id);

        if (domain == null) {
            throw BusinessException.recordNotFound("domain: " + id);
        }

        return domain;
    }

    /**
     * Require a non-blank parent id to reference an existing domain.
     *
     * @param parentId parent domain id, blank for root
     */
    private void requireParent(String parentId) {
        if (StringUtil011.isBlank(parentId)) {
            return;
        }

        if (repository.findById(parentId) == null) {
            throw BusinessException.badRequest("parent domain not found, id=" + parentId);
        }
    }

    /**
     * Require an existing and enabled domain.
     *
     * @param pkMt domain id
     * @return enabled domain
     */
    private JulyAiDomain requireEnabledDomain(String pkMt) {
        JulyAiDomain domain = repository.findById(pkMt);

        if (domain == null) {
            throw BusinessException.badRequest("domain not found, id=" + pkMt);
        }

        if (!Status011.ENABLED.getCode().equals(domain.status())) {
            throw BusinessException.badRequest("domain is disabled, id=" + pkMt);
        }

        return domain;
    }

    /**
     * Require an existing prompt by id.
     *
     * @param id prompt id
     * @return prompt
     */
    private JulyAiDomainPrompt requirePrompt(String id) {
        JulyAiDomainPrompt prompt = promptRepository.findById(id);

        if (prompt == null) {
            throw BusinessException.recordNotFound("prompt: " + id);
        }

        return prompt;
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
