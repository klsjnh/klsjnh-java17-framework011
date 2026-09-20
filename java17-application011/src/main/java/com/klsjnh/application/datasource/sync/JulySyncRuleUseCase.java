package com.klsjnh.application.datasource.sync;

/*                JulySyncRuleUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync rule use case (crud + run)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.datasource.sync.JulySyncRule;
import com.klsjnh.domain.datasource.sync.JulySyncRuleColumn;
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
 * rule once. Transaction boundary is here.
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
     * Create the use case.
     *
     * @param ruleRepository   rule repository
     * @param columnRepository column repository
     * @param syncEngine       sync engine
     */
    public JulySyncRuleUseCase(JulySyncRuleRepository ruleRepository,
            JulySyncRuleColumnRepository columnRepository, SyncEnginePort syncEngine) {
        this.ruleRepository = ruleRepository;
        this.columnRepository = columnRepository;
        this.syncEngine = syncEngine;
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
    public String insert(String syncCode, String syncName, String sourceDsCode, String sourceKind, String sourceData,
            String targetDsCode, String targetKind, String targetData, String mode, String syncKey, String conflict,
            Integer pageSize, String remark, List<SyncColumnCommand> columns) {
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
    public JulySyncRule getByCode(String syncCode) {
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
    public List<JulySyncRuleColumn> columns(String pkMt) {
        return columnRepository.findByMaster(pkMt);
    }

    /**
     * Page query.
     *
     * @param query page query
     * @param spec  filter
     * @return page result
     */
    public PageResult011<JulySyncRule> selectListByPage(PageQuery011 query, JulySyncRuleQuerySpec spec) {
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
    public String logicDelete(String id) {
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
    public SyncRunResult run(String syncCode) {
        return syncEngine.run(getByCode(syncCode));
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
