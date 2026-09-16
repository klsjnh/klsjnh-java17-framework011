package com.klsjnh.application.dataservice011;

/*                JulyBusinessModelingUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling use case class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.vo.BatchDeleteErrorVo011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.domain.dataservice011.BusinessModelingProbePort;
import com.klsjnh.domain.dataservice011.FieldInferencePort;
import com.klsjnh.domain.dataservice011.JulyBusinessModeling;
import com.klsjnh.domain.dataservice011.JulyBusinessModelingQuerySpec;
import com.klsjnh.domain.dataservice011.JulyBusinessModelingRepository;
import com.klsjnh.domain.dataservice011.JulyDatasource;
import com.klsjnh.domain.dataservice011.JulyDatasourceRepository;
import com.klsjnh.domain.dataservice011.ModelingSqlGuard;
import com.klsjnh.domain.dataservice011.ProbeOutcome;
import com.klsjnh.domain.datasource.SqlRoutingPort;
import com.klsjnh.domain.lowcode011.enums.ObjectType011;
import com.klsjnh.domain.lowcode011.model.FieldInfo011;
import com.klsjnh.domain.lowcode011.model.MetaData011;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JulyBusinessModeling use cases: modeling CRUD, the SQL probe inference, the
 * direct and paged SQL execution against the bound datasource, and the
 * hand-over product read (`getModelData`).
 * <p>
 * The probe and the two execute actions run on the BUSINESS datasource and are
 * therefore NOT transactional (they never join the primary transaction), the
 * same stance as the datasource topic's connectivity test.
 * </p>
 */

@Service
public class JulyBusinessModelingUseCase {

    /**
     * Row cap for the non-paged execute action; beyond it the caller is told to
     * use the paged endpoint.
     */
    private static final int EXECUTE_ROW_CAP = 500;

    /**
     * Modeling repository.
     */
    private final JulyBusinessModelingRepository repository;

    /**
     * Datasource repository (existence / enabled checks on dataSourceCode).
     */
    private final JulyDatasourceRepository datasourceRepository;

    /**
     * Probe port (reads ResultSetMetaData).
     */
    private final BusinessModelingProbePort probePort;

    /**
     * Field inference port.
     */
    private final FieldInferencePort inferencePort;

    /**
     * SQL routing port (business datasource reads).
     */
    private final SqlRoutingPort sqlRoutingPort;

    /**
     * Create the use case.
     *
     * @param repository           modeling repository
     * @param datasourceRepository datasource repository
     * @param probePort            probe port
     * @param inferencePort        field inference port
     * @param sqlRoutingPort       sql routing port
     */
    public JulyBusinessModelingUseCase(JulyBusinessModelingRepository repository,
            JulyDatasourceRepository datasourceRepository, BusinessModelingProbePort probePort,
            FieldInferencePort inferencePort, SqlRoutingPort sqlRoutingPort) {
        this.repository = repository;
        this.datasourceRepository = datasourceRepository;
        this.probePort = probePort;
        this.inferencePort = inferencePort;
        this.sqlRoutingPort = sqlRoutingPort;
    }

    /**
     * Insert a new modeling entry.
     *
     * @param modelCode         modeling code, unique, immutable
     * @param modelName         modeling name
     * @param dataSourceCode    datasource code, must be enabled
     * @param sqlContent           take-out sql, optional
     * @param objectName        object name, unique, immutable
     * @param objectType        object type code
     * @param objectDescription object description
     * @param packageName       target package name
     * @param businessField     business field mapping
     * @param routerPath        route path
     * @param remark            remark, optional
     * @param fields            field definitions
     * @return new modeling id
     */
    @Transactional
    public String insert(String modelCode, String modelName, String dataSourceCode, String sqlContent, String objectName,
            String objectType, String objectDescription, String packageName, String businessField, String routerPath,
            String remark, List<FieldInfo011> fields) {
        requireEnabledDatasource(dataSourceCode);

        if (repository.findByCode(modelCode) != null) {
            throw BusinessException.badRequest("modeling code already exists: " + modelCode);
        }

        if (repository.findByObjectName(objectName) != null) {
            throw BusinessException.badRequest("object name already exists: " + objectName);
        }

        JulyBusinessModeling modeling = newModeling(modelCode, modelName, dataSourceCode, sqlContent, objectName,
                objectType, objectDescription, packageName, businessField, routerPath, remark, fields);
        repository.insert(modeling);

        return modeling.id().value();
    }

    /**
     * Update a modeling entry; the modeling code and object name are immutable.
     *
     * @param id                modeling id
     * @param modelName         modeling name
     * @param dataSourceCode    datasource code, must be enabled
     * @param sqlContent           take-out sql, optional
     * @param objectType        object type code
     * @param objectDescription object description
     * @param packageName       target package name
     * @param businessField     business field mapping
     * @param routerPath        route path
     * @param remark            remark, optional
     * @param fields            field definitions (replace all)
     * @return modeling id
     */
    @Transactional
    public String update(String id, String modelName, String dataSourceCode, String sqlContent, String objectType,
            String objectDescription, String packageName, String businessField, String routerPath, String remark,
            List<FieldInfo011> fields) {
        JulyBusinessModeling modeling = require(id);
        requireEnabledDatasource(dataSourceCode);

        try {
            modeling.updateBasics(modelName, dataSourceCode, sqlContent, remark);
            modeling.updateMetaBasics(ObjectType011.fromString(objectType), objectDescription, businessField,
                    packageName, routerPath);
            modeling.replaceFields(fields);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        repository.update(modeling);

        return modeling.id().value();
    }

    /**
     * Logic delete a modeling entry.
     *
     * @param id modeling id
     * @return deleted modeling id
     */
    @Transactional
    public String logicDelete(String id) {
        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Batch logic delete with a per-id success/failure summary.
     *
     * @param ids modeling ids
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
     * Find by primary key.
     *
     * @param id modeling id
     * @return aggregate
     */
    public JulyBusinessModeling getById(String id) {
        return require(id);
    }

    /**
     * Find by modeling code.
     *
     * @param modelCode modeling code
     * @return aggregate
     */
    public JulyBusinessModeling getByCode(String modelCode) {
        JulyBusinessModeling modeling = repository.findByCode(modelCode);

        if (modeling == null) {
            throw BusinessException.recordNotFound(modelCode);
        }

        return modeling;
    }

    /**
     * Page query on the management view.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param spec      query condition, null means no filter
     * @return page result
     */
    public PageResult011<JulyBusinessModeling> selectListByPage(PageQuery011 pageQuery,
            JulyBusinessModelingQuerySpec spec) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        JulyBusinessModelingQuerySpec condition = spec == null
                ? new JulyBusinessModelingQuerySpec(null, null, null)
                : spec;
        List<JulyBusinessModeling> rows = repository.findPage(query.offset(), query.pageSize(), condition);
        long total = repository.count(condition);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Probe a draft SQL and infer the field list (nothing is persisted).
     *
     * @param dataSourceCode datasource code, must be enabled
     * @param sqlContent        read-only sql
     * @param objectName     object name, informational
     * @return probe result
     */
    public BusinessModelingProbeResult probeAndInfer(String dataSourceCode, String sqlContent, String objectName) {
        requireEnabledDatasource(dataSourceCode);
        guardSql(sqlContent);

        ProbeOutcome outcome = probePort.probe(dataSourceCode, sqlContent);

        if (!outcome.success()) {
            return BusinessModelingProbeResult.fail(outcome.message());
        }

        return BusinessModelingProbeResult.ok(inferencePort.infer(outcome));
    }

    /**
     * Execute a read-only SQL against the bound business datasource.
     *
     * @param dataSourceCode datasource code, must be enabled
     * @param sqlContent        read-only sql
     * @return rows, never null
     */
    public List<Map<String, Object>> executeSql(BusinessModelingExecuteCommand command) {
        Resolved resolved = resolve(command);
        guardSql(resolved.sql());

        PageResult011<Map<String, Object>> page = sqlRoutingPort.selectListByPage(resolved.dsCode(),
                resolved.sql(), 1, EXECUTE_ROW_CAP);

        if (page.total() > EXECUTE_ROW_CAP) {
            throw BusinessException.badRequest("result exceeds " + EXECUTE_ROW_CAP
                    + " rows, use executeSqlByPage instead");
        }

        return page.rows();
    }

    /**
     * Execute a read-only SQL with dialect paging against the bound business
     * datasource.
     *
     * @param dataSourceCode datasource code, must be enabled
     * @param sqlContent        read-only sql
     * @param pageIndex      page index starting at 1
     * @param pageSize       page size, clamped by the routing port to [10, 500]
     * @return page result
     */
    public PageResult011<Map<String, Object>> executeSqlByPage(BusinessModelingExecuteCommand command,
            Integer pageIndex, Integer pageSize) {
        Resolved resolved = resolve(command);
        guardSql(resolved.sql());

        return sqlRoutingPort.selectListByPage(resolved.dsCode(), resolved.sql(), pageIndex, pageSize);
    }

    /**
     * Resolve the polymorphic execute input to a concrete (sql, datasource code)
     * pair: exactly one SQL source, at most one datasource selector, and the
     * datasource derived from the modeling when none is given.
     *
     * @param command execute command
     * @return resolved sql + datasource code
     */
    private Resolved resolve(BusinessModelingExecuteCommand command) {
        boolean byModelId = isPresent(command.modelId());
        boolean byModelCode = isPresent(command.modelCode());
        boolean bySql = isPresent(command.sqlContent());

        if ((byModelId ? 1 : 0) + (byModelCode ? 1 : 0) + (bySql ? 1 : 0) != 1) {
            throw BusinessException.badRequest("exactly one of modelId / modelCode / sqlContent is required");
        }

        JulyBusinessModeling model = null;
        String sql;

        if (byModelId) {
            model = require(command.modelId());
            sql = modelSql(model);
        } else if (byModelCode) {
            model = repository.findByCode(command.modelCode());
            if (model == null) {
                throw BusinessException.recordNotFound(command.modelCode());
            }
            sql = modelSql(model);
        } else {
            sql = command.sqlContent().trim();
        }

        if (isPresent(command.dataSourceId()) && isPresent(command.dataSourceCode())) {
            throw BusinessException.badRequest("only one of dataSourceId / dataSourceCode is allowed");
        }

        String dsCode = null;

        if (isPresent(command.dataSourceId())) {
            JulyDatasource datasource = datasourceRepository.findById(command.dataSourceId());
            if (datasource == null) {
                throw BusinessException.recordNotFound(command.dataSourceId());
            }
            dsCode = datasource.dsCode();
        } else if (isPresent(command.dataSourceCode())) {
            dsCode = command.dataSourceCode().trim();
        } else if (model != null) {
            dsCode = model.dataSourceCode();
        }

        if (!isPresent(dsCode)) {
            throw BusinessException.badRequest("dataSourceId / dataSourceCode is required");
        }

        requireEnabledDatasource(dsCode);

        return new Resolved(sql, dsCode);
    }

    /**
     * Read the SQL of a modeling, rejecting a modeling that carries none.
     *
     * @param model modeling aggregate
     * @return sql content
     */
    private String modelSql(JulyBusinessModeling model) {
        if (!isPresent(model.sqlContent())) {
            throw BusinessException.badRequest("modeling has no sql: " + model.modelCode());
        }

        return model.sqlContent();
    }

    /**
     * Whether a selector carries a value.
     *
     * @param value raw selector
     * @return true when non-blank
     */
    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * A resolved (sql, datasource code) pair.
     *
     * @param sql    read-only sql
     * @param dsCode datasource code
     */
    private record Resolved(String sql, String dsCode) {
    }

    /**
     * Hand-over product read: the aggregate with its description and fields.
     *
     * @param id modeling id
     * @return aggregate
     */
    public JulyBusinessModeling getModelData(String id) {
        return require(id);
    }

    /**
     * Validate the take-out SQL with the shared read-only guard.
     *
     * @param sqlContent raw sql
     */
    private void guardSql(String sqlContent) {
        try {
            ModelingSqlGuard.validate(sqlContent);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Require the referenced datasource to exist and be enabled.
     *
     * @param dataSourceCode datasource code
     */
    private void requireEnabledDatasource(String dataSourceCode) {
        JulyDatasource datasource = datasourceRepository.findByCode(dataSourceCode);

        if (datasource == null || !Status011.ENABLED.getCode().equals(datasource.status())) {
            throw BusinessException.badRequest("datasource is not available: " + dataSourceCode);
        }
    }

    /**
     * Build a new aggregate, translating domain validation failures into 400.
     *
     * @param modelCode         modeling code
     * @param modelName         modeling name
     * @param dataSourceCode    datasource code
     * @param sqlContent           take-out sql
     * @param objectName        object name
     * @param objectType        object type code
     * @param objectDescription object description
     * @param packageName       target package name
     * @param businessField     business field mapping
     * @param routerPath        route path
     * @param remark            remark
     * @param fields            field definitions
     * @return new aggregate
     */
    private JulyBusinessModeling newModeling(String modelCode, String modelName, String dataSourceCode, String sqlContent,
            String objectName, String objectType, String objectDescription, String packageName, String businessField,
            String routerPath, String remark, List<FieldInfo011> fields) {
        try {
            MetaData011 metaData = MetaData011.create(objectName, ObjectType011.fromString(objectType),
                    objectDescription, businessField, packageName, routerPath);

            for (FieldInfo011 field : fields == null ? List.<FieldInfo011>of() : fields) {
                metaData.addField(field);
            }

            return JulyBusinessModeling.create(EntityId.generate(), modelCode, modelName, metaData, dataSourceCode,
                    sqlContent, remark, AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Require an existing modeling entry.
     *
     * @param id modeling id
     * @return aggregate
     */
    private JulyBusinessModeling require(String id) {
        JulyBusinessModeling modeling = repository.findById(id);

        if (modeling == null) {
            throw BusinessException.recordNotFound(id);
        }

        return modeling;
    }

    /**
     * Normalize a batch id list: trim, drop blanks, deduplicate keeping order.
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
