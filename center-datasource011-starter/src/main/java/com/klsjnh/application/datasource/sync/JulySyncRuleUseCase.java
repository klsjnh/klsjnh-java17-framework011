package com.klsjnh.application.datasource.sync;

/*                JulySyncRuleUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync rule use case (crud + run)
 *      2026.09.26  explicit permission checks (julySyncRule auth)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.datasource.sync.JulySyncRule;
import com.klsjnh.domain.datasource.sync.JulySyncRuleColumn;
import com.klsjnh.domain.datasource.sync.JulySyncRulePermissionCodes011;
import com.klsjnh.domain.datasource.sync.JulySyncRuleColumnRepository;
import com.klsjnh.domain.datasource.sync.JulySyncRuleQuerySpec;
import com.klsjnh.domain.datasource.sync.JulySyncRuleRepository;
import com.klsjnh.domain.datasource.sync.SyncEnginePort;
import com.klsjnh.domain.datasource.sync.SyncRunResult;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sync rule use case: rule CRUD (with the column mapping child) and running a
 * rule once. Management actions assert permission codes via
 * {@link AuthorizationPort}. Transaction boundary is here.
 */

@Service
public class JulySyncRuleUseCase {

    /**
     * Rule repository.
     */
    private final JulySyncRuleRepository ruleRepository;

    /**
     * Column repository.
     */
    private final JulySyncRuleColumnRepository columnRepository;

    /**
     * Sync engine.
     */
    private final SyncEnginePort syncEngine;

    /**
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * Create the use case.
     *
     * @param ruleRepository     rule repository
     * @param columnRepository   column repository
     * @param syncEngine         sync engine
     * @param authorizationPort  authorization port
     */
    public JulySyncRuleUseCase(JulySyncRuleRepository ruleRepository,
            JulySyncRuleColumnRepository columnRepository, SyncEnginePort syncEngine,
            AuthorizationPort authorizationPort) {
        this.ruleRepository = ruleRepository;
        this.columnRepository = columnRepository;
        this.syncEngine = syncEngine;
        this.authorizationPort = authorizationPort;
    }

    /**
     * Insert a rule with its column mapping.
     *
     * @param syncCode     sync code
     * @param syncName     sync name
     * @param sourceDsCode source datasource code
     * @param sourceKind   source kind
     * @param sourceData   source data
     * @param targetDsCode target datasource code
     * @param targetKind   target kind
     * @param targetData   target data
     * @param mode         mode
     * @param syncKey      business key columns
     * @param conflict     conflict strategy
     * @param pageSize     page size
     * @param remark       remark
     * @param columns      column mapping
     * @return new rule id
     */
    @Transactional
    public String insert(String operatorId, String syncCode, String syncName, String sourceDsCode, String sourceKind,
            String sourceData, String targetDsCode, String targetKind, String targetData, String mode, String syncKey,
            String conflict, Integer pageSize, String remark, List<SyncColumnCommand> columns) {
        authorizationPort.assertHas(operatorId, JulySyncRulePermissionCodes011.INSERT);
        String id = EntityId.generate().value();
        JulySyncRule rule = JulySyncRule.create(EntityId.of(id), syncCode, syncName, sourceDsCode, sourceKind, sourceData,
                targetDsCode, targetKind, targetData, mode, syncKey, conflict, pageSize, remark, AuditInfo.empty());
        ruleRepository.insert(rule);
        insertColumns(id, columns);

        return id;
    }

    /**
     * Find a rule by sync code.
     *
     * @param syncCode sync code
     * @return rule
     */
    public JulySyncRule getByCode(String operatorId, String syncCode) {
        authorizationPort.assertHas(operatorId, JulySyncRulePermissionCodes011.SELECT);
        JulySyncRule rule = ruleRepository.findByCode(syncCode);

        if (rule == null) {
            throw BusinessException.recordNotFound("sync rule: " + syncCode);
        }

        return rule;
    }

    /**
     * The column mapping of a rule.
     *
     * @param pkMt rule id
     * @return column mapping
     */
    public List<JulySyncRuleColumn> columns(String operatorId, String pkMt) {
        authorizationPort.assertHas(operatorId, JulySyncRulePermissionCodes011.SELECT);
        return columnRepository.findByMaster(pkMt);
    }

    /**
     * Whole save (rule + column mappings, one transaction): a blank id inserts
     * the rule and a present id updates it, then the old columns are logically
     * deleted and the given list is inserted (Replace strategy).
     *
     * @param id           rule id, blank inserts a new rule
     * @param syncCode     sync code, used on insert only
     * @param syncName     sync name
     * @param sourceDsCode source datasource code
     * @param sourceKind   source kind
     * @param sourceData   source data
     * @param targetDsCode target datasource code
     * @param targetKind   target kind
     * @param targetData   target data
     * @param mode         mode
     * @param syncKey      business key columns
     * @param conflict     conflict strategy
     * @param pageSize     page size
     * @param remark       remark
     * @param status       row status, null keeps the stored one
     * @param columns      column mappings replacing the old children, nullable
     * @return rule id
     */
    @Transactional
    public String saveWhole(String operatorId, String id, String syncCode, String syncName, String sourceDsCode,
            String sourceKind, String sourceData, String targetDsCode, String targetKind, String targetData,
            String mode, String syncKey, String conflict, Integer pageSize, String remark, String status,
            List<SyncColumnCommand> columns) {
        if (id == null || id.isBlank()) {
            authorizationPort.assertHas(operatorId, JulySyncRulePermissionCodes011.INSERT);
        } else {
            authorizationPort.assertHas(operatorId, JulySyncRulePermissionCodes011.UPDATE);
        }

        String ruleId = id == null || id.isBlank() ? EntityId.generate().value() : id;

        if (id == null || id.isBlank()) {
            JulySyncRule rule = JulySyncRule.create(EntityId.of(ruleId), syncCode, syncName, sourceDsCode, sourceKind,
                    sourceData, targetDsCode, targetKind, targetData, mode, syncKey, conflict, pageSize, remark,
                    AuditInfo.empty());
            ruleRepository.insert(rule);
        } else {
            JulySyncRule rule = ruleRepository.findById(ruleId);
            if (rule == null) {
                throw BusinessException.recordNotFound("sync rule: " + ruleId);
            }
            rule.update(syncName, sourceDsCode, sourceKind, sourceData, targetDsCode, targetKind, targetData, mode,
                    syncKey, conflict, pageSize, remark, status);
            ruleRepository.update(rule);
        }

        columnRepository.logicDeleteByMaster(ruleId);
        insertColumns(ruleId, columns);

        return ruleId;
    }

    /**
     * Read a rule together with its column mappings (master + children).
     *
     * @param id rule id
     * @return map with {@code "master"} and {@code "columns"}
     */
    public java.util.Map<String, Object> getWithChildren(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulySyncRulePermissionCodes011.SELECT);
        JulySyncRule rule = ruleRepository.findById(id);

        if (rule == null) {
            throw BusinessException.recordNotFound("sync rule: " + id);
        }

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("master", rule);
        result.put("columns", columnRepository.findByMaster(id));

        return result;
    }

    /**
     * Page query.
     *
     * @param query page query
     * @param spec  filter
     * @return page result
     */
    public PageResult011<JulySyncRule> selectListByPage(String operatorId, PageQuery011 query,
            JulySyncRuleQuerySpec spec) {
        authorizationPort.assertHas(operatorId, JulySyncRulePermissionCodes011.SELECT);
        long total = ruleRepository.count(spec);
        List<JulySyncRule> rows = ruleRepository.findPage(query.offset(), query.pageSize(), spec);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Logic delete a rule and its column mapping.
     *
     * @param id rule id
     * @return the deleted id
     */
    @Transactional
    public String logicDelete(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulySyncRulePermissionCodes011.LOGIC_DELETE);
        JulySyncRule rule = ruleRepository.findById(id);

        if (rule == null) {
            throw BusinessException.recordNotFound("sync rule: " + id);
        }

        columnRepository.logicDeleteByMaster(id);
        ruleRepository.logicDeleteById(id);

        return id;
    }

    /**
     * Run a rule once.
     *
     * @param syncCode sync code
     * @return run result
     */
    public SyncRunResult run(String operatorId, String syncCode) {
        authorizationPort.assertHas(operatorId, JulySyncRulePermissionCodes011.RUN);
        JulySyncRule rule = ruleRepository.findByCode(syncCode);

        if (rule == null) {
            throw BusinessException.recordNotFound("sync rule: " + syncCode);
        }

        return syncEngine.run(rule);
    }

    /**
     * Insert the column mapping rows.
     *
     * @param pkMt    rule id
     * @param columns column commands
     */
    private void insertColumns(String pkMt, List<SyncColumnCommand> columns) {
        if (columns == null || columns.isEmpty()) {
            return;
        }

        int order = 1;

        for (SyncColumnCommand column : columns) {
            JulySyncRuleColumn row = JulySyncRuleColumn.create(EntityId.generate(), pkMt, column.sourceColumn(),
                    column.sourceType(), column.targetColumn(), column.targetType(), column.transform(),
                    column.sortOrder() == null ? order : column.sortOrder(), null, AuditInfo.empty());
            columnRepository.insert(row);
            order++;
        }
    }
}
