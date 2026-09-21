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
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.aicenter.prompt.JulyAiDomain;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPromptRepository;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainQuerySpec;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JulyAiDomain use cases: business domain tree CRUD, page query and tree
 * assembly. A domain can be deleted only when it has neither alive children
 * nor prompts.
 */

@Service
public class JulyAiDomainUseCase {

    /**
     * Domain repository.
     */
    private final JulyAiDomainRepository repository;

    /**
     * Prompt repository (delete guard).
     */
    private final JulyAiDomainPromptRepository promptRepository;

    /**
     * Create the use case.
     *
     * @param repository       domain repository
     * @param promptRepository prompt repository
     */
    public JulyAiDomainUseCase(JulyAiDomainRepository repository, JulyAiDomainPromptRepository promptRepository) {
        this.repository = repository;
        this.promptRepository = promptRepository;
    }

    /**
     * Insert a new domain node.
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
     * Update a domain (domainCode is immutable).
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
     * Assemble the enabled domain tree (children nested, ordered by
     * sort_order / id).
     *
     * @return root nodes with nested children
     */
    public List<JulyAiDomain> selectTree() {
        List<JulyAiDomain> rows = repository.findAllEnabled();
        Map<String, JulyAiDomain> byId = new HashMap<>();

        for (JulyAiDomain row : rows) {
            byId.put(row.id().value(), row);
        }

        List<JulyAiDomain> roots = new ArrayList<>();

        for (JulyAiDomain row : rows) {
            JulyAiDomain parent = StringUtil011.isBlank(row.parentId()) ? null : byId.get(row.parentId());

            if (parent == null) {
                roots.add(row);
            } else {
                parent.addChild(row);
            }
        }

        return roots;
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
     * Require a non-blank parent id to reference an existing enabled or
     * disabled domain.
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
}
