package com.klsjnh.application.messagecenter.outbound.template;

/*                JulyOutboundTemplateUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template use case class
 *      2026.09.26  explicit permission checks (julyMessageOutboundTemplate)
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.domain.messagecenter.outbound.template.JulyOutboundTemplate;
import com.klsjnh.domain.messagecenter.outbound.template.JulyMessageOutboundTemplatePermissionCodes011;
import com.klsjnh.domain.messagecenter.outbound.template.JulyOutboundTemplateQuerySpec;
import com.klsjnh.domain.messagecenter.outbound.template.JulyOutboundTemplateRepository;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JulyOutboundTemplate use cases: message template CRUD.
 */

@Service
public class JulyOutboundTemplateUseCase {

    /**
     * Template repository.
     */
    private final JulyOutboundTemplateRepository repository;

    private final AuthorizationPort authorizationPort;

    public JulyOutboundTemplateUseCase(JulyOutboundTemplateRepository repository,
            AuthorizationPort authorizationPort) {
        this.repository = repository;
        this.authorizationPort = authorizationPort;
    }

    /**
     * Insert a new template.
     *
     * @param templateCode template code, unique, immutable
     * @param sortOrder    manual sort order, null falls back to the default
     * @param templateName template display name
     * @param channelCode  channel code
     * @param title        title, optional
     * @param content      content body
     * @param remark       remark, optional
     * @return new template id
     */
    @Transactional
    public String insert(String operatorId, String templateCode, Integer sortOrder, String templateName,
            String channelCode, String title, String content, String remark) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundTemplatePermissionCodes011.INSERT);
        if (repository.findByCode(templateCode) != null) {
            throw BusinessException.badRequest("template code already exists: " + templateCode);
        }

        JulyOutboundTemplate template = newTemplate(templateCode, sortOrder, templateName, channelCode, title, content,
                remark);
        repository.insert(template);

        return template.id().value();
    }

    /**
     * Update a template (templateCode is immutable).
     *
     * @param id           template id
     * @param templateName template display name
     * @param channelCode  channel code
     * @param title        title
     * @param content      content body
     * @param sortOrder    manual sort order, null keeps the stored one
     * @param status       row status, null keeps the stored one
     * @param remark       remark
     * @return template id
     */
    @Transactional
    public String update(String operatorId, String id, String templateName, String channelCode, String title,
            String content, Integer sortOrder, String status, String remark) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundTemplatePermissionCodes011.UPDATE);
        JulyOutboundTemplate template = require(id);
        requireStatus(status);

        try {
            template.update(templateName, channelCode, title, content, sortOrder, status, remark);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        repository.update(template);

        return template.id().value();
    }

    /**
     * Logic delete a template.
     *
     * @param id template id
     * @return deleted template id
     */
    @Transactional
    public String logicDelete(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundTemplatePermissionCodes011.LOGIC_DELETE);
        require(id);

        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Batch logic delete, all-or-nothing: a missing id fails the whole batch.
     *
     * @param ids template ids
     * @return batch delete summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(String operatorId, List<String> ids) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundTemplatePermissionCodes011.LOGIC_DELETE);
        List<String> normalized = normalize(ids);

        if (normalized.isEmpty()) {
            throw BusinessException.badRequest("batch logic delete: ids is required");
        }

        repository.logicDeleteByIds(normalized);

        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        result.setTotal(normalized.size());
        result.setSuccess(normalized.size());
        result.setFailed(0);

        return result;
    }

    /**
     * Find a template by primary key.
     *
     * @param id template id
     * @return aggregate
     */
    public JulyOutboundTemplate getById(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundTemplatePermissionCodes011.SELECT);
        return require(id);
    }

    /**
     * Page query on the management view.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param spec      query condition, null means no filter
     * @return page result
     */
    public PageResult011<JulyOutboundTemplate> selectListByPage(String operatorId, PageQuery011 pageQuery,
            JulyOutboundTemplateQuerySpec spec) {
        authorizationPort.assertHas(operatorId, JulyMessageOutboundTemplatePermissionCodes011.SELECT);
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        JulyOutboundTemplateQuerySpec condition = spec == null ? new JulyOutboundTemplateQuerySpec(null, null, null) : spec;
        List<JulyOutboundTemplate> rows = repository.findPage(query.offset(), query.pageSize(), condition);
        long total = repository.count(condition);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Build a new aggregate, translating domain validation failures into 400.
     *
     * @param templateCode template code
     * @param sortOrder    manual sort order
     * @param templateName template display name
     * @param channelCode  channel code
     * @param title        title
     * @param content      content body
     * @param remark       remark
     * @return new aggregate
     */
    private JulyOutboundTemplate newTemplate(String templateCode, Integer sortOrder, String templateName,
            String channelCode, String title, String content, String remark) {
        try {
            return JulyOutboundTemplate.create(EntityId.generate(), templateCode, sortOrder, templateName, channelCode,
                    title, content, remark, AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Require an existing template.
     *
     * @param id template id
     * @return aggregate
     */
    private JulyOutboundTemplate require(String id) {
        JulyOutboundTemplate template = repository.findById(id);

        if (template == null) {
            throw BusinessException.recordNotFound(id);
        }

        return template;
    }

    /**
     * Reject an unknown status when one is supplied.
     *
     * @param status raw status, nullable
     */
    private void requireStatus(String status) {
        if (status != null && !status.isBlank() && Status011.of(status) == null) {
            throw BusinessException.badRequest("unknown status: " + status);
        }
    }

    /**
     * Normalize a batch id list.
     *
     * @param ids raw ids
     * @return normalized ids, never null
     */
    private List<String> normalize(List<String> ids) {
        return ids == null ? List.of()
                : ids.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).distinct().toList();
    }
}
