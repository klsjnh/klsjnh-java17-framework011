package com.klsjnh.application.lowcode011;

/*                JulyMetadataUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata use case class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.vo.BatchDeleteErrorVo011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataDisplay;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataQuerySpec;
import com.klsjnh.domain.lowcode011.JulyMetadataRepository;
import com.klsjnh.domain.lowcode011.JulyMetadataService;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * JulyMetadata use cases: object metadata CRUD with the three child
 * collections (fields / displays / services), replace strategy on update.
 */

@Service
public class JulyMetadataUseCase {

    /**
     * Metadata repository.
     */
    private final JulyMetadataRepository repository;

    /**
     * Create the use case.
     *
     * @param repository metadata repository
     */
    public JulyMetadataUseCase(JulyMetadataRepository repository) {
        this.repository = repository;
    }

    /**
     * Insert a new object with its children.
     *
     * @param objectName    object name, unique, immutable
     * @param sortOrder     manual sort order
     * @param objectType    object type code
     * @param description   object description
     * @param businessField business field mapping
     * @param packageName   target package name
     * @param routerPath    route path
     * @param remark        remark
     * @param fields        fields
     * @param displays      displays
     * @param services      services
     * @return new object id
     */
    @Transactional
    public String insert(String objectName, Integer sortOrder, String objectType, String description,
            String businessField, String packageName, String routerPath, String remark, List<JulyMetadataField> fields,
            List<JulyMetadataDisplay> displays, List<JulyMetadataService> services) {
        if (businessField == null || businessField.isBlank()) {
            throw BusinessException.badRequest("businessField required (business key)");
        }

        if (repository.findByObjectName(objectName) != null) {
            throw BusinessException.badRequest("object name already exists: " + objectName);
        }

        JulyMetadata metadata = newMetadata(objectName, sortOrder, objectType, description, businessField, packageName,
                routerPath, remark);

        try {
            metadata.replaceChildren(fields, displays, services);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        repository.insert(metadata);

        return metadata.id().value();
    }

    /**
     * Update an object (objectName immutable; children replaced).
     *
     * @param id            object id
     * @param objectType    object type code
     * @param description   object description
     * @param businessField business field mapping
     * @param packageName   target package name
     * @param routerPath    route path
     * @param sortOrder     manual sort order, null keeps the stored one
     * @param status        row status, null keeps the stored one
     * @param remark        remark
     * @param fields        fields
     * @param displays      displays
     * @param services      services
     * @return object id
     */
    @Transactional
    public String update(String id, String objectType, String description, String businessField, String packageName,
            String routerPath, Integer sortOrder, String status, String remark, List<JulyMetadataField> fields,
            List<JulyMetadataDisplay> displays, List<JulyMetadataService> services) {
        if (businessField == null || businessField.isBlank()) {
            throw BusinessException.badRequest("businessField required (business key)");
        }

        JulyMetadata metadata = require(id);
        requireStatus(status);

        try {
            metadata.update(objectType, description, businessField, packageName, routerPath, sortOrder, status,
                    remark);
            metadata.replaceChildren(fields, displays, services);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        repository.update(metadata);

        return metadata.id().value();
    }

    /**
     * Logic delete an object and its children.
     *
     * @param id object id
     * @return deleted object id
     */
    @Transactional
    public String logicDelete(String id) {
        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Batch logic delete with a per-id summary.
     *
     * @param ids object ids
     * @return per-id summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(List<String> ids) {
        List<String> normalized = normalize(ids);

        if (normalized.isEmpty()) {
            throw BusinessException.badRequest("batch logic delete: ids is required");
        }

        int deleted = 0;
        List<String> missing = new ArrayList<>();

        for (String id : normalized) {
            if (repository.logicDeleteById(id)) {
                deleted++;
            } else {
                missing.add(id);
            }
        }

        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        result.setTotal(normalized.size());
        result.setSuccess(deleted);
        result.setFailed(missing.size());

        for (String id : missing) {
            BatchDeleteErrorVo011 error = new BatchDeleteErrorVo011();
            error.setId(id);
            error.setMessage("record not found");
            result.getErrors().add(error);
        }

        return result;
    }

    /**
     * Find an object by primary key (with children).
     *
     * @param id object id
     * @return aggregate
     */
    public JulyMetadata getById(String id) {
        return require(id);
    }

    /**
     * Find an object by object name (with children).
     *
     * @param objectName object name
     * @return aggregate
     */
    public JulyMetadata getByObjectName(String objectName) {
        JulyMetadata metadata = repository.findByObjectName(objectName);

        if (metadata == null) {
            throw BusinessException.recordNotFound(objectName);
        }

        return metadata;
    }

    /**
     * Page query on the master (children not loaded).
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param spec      query condition, null means no filter
     * @return page result
     */
    public PageResult011<JulyMetadata> selectListByPage(PageQuery011 pageQuery, JulyMetadataQuerySpec spec) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        JulyMetadataQuerySpec condition = spec == null ? new JulyMetadataQuerySpec(null, null, null) : spec;
        List<JulyMetadata> rows = repository.findPage(query.offset(), query.pageSize(), condition);
        long total = repository.count(condition);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Build a new aggregate, translating validation failures into 400.
     *
     * @param objectName    object name
     * @param sortOrder     manual sort order
     * @param objectType    object type code
     * @param description   object description
     * @param businessField business field mapping
     * @param packageName   target package name
     * @param routerPath    route path
     * @param remark        remark
     * @return new aggregate
     */
    private JulyMetadata newMetadata(String objectName, Integer sortOrder, String objectType, String description,
            String businessField, String packageName, String routerPath, String remark) {
        try {
            return JulyMetadata.create(EntityId.generate(), objectName, sortOrder, objectType, description,
                    businessField, packageName, routerPath, remark, AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Require an existing object.
     *
     * @param id object id
     * @return aggregate
     */
    private JulyMetadata require(String id) {
        JulyMetadata metadata = repository.findById(id);

        if (metadata == null) {
            throw BusinessException.recordNotFound(id);
        }

        return metadata;
    }

    /**
     * Reject an unknown status when supplied.
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
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> seen = new LinkedHashSet<>();

        for (String id : ids) {
            if (id != null && !id.isBlank()) {
                seen.add(id.trim());
            }
        }

        return new ArrayList<>(seen);
    }
}
