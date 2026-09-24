package com.klsjnh.application.system011.config;

/*                JulyConfigUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate 2026.09.24
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config use case class
 *      2026.09.24  explicit permission checks (julyConfig auth demo)
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.config.JulyConfig;
import com.klsjnh.domain.system011.config.JulyConfigPermissionCodes011;
import com.klsjnh.domain.system011.config.JulyConfigRepository;
import com.klsjnh.application.platform011.backup.BackupUseCase;
import com.klsjnh.application.platform011.export.ExportUseCase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JulyConfig use cases: admin CRUD over runtime key-value parameters, plus
 * getByCode — the program-facing read entry (no HTTP endpoint; readers query
 * the store every time, no cache). Management actions assert permission codes
 * via {@link AuthorizationPort} (IAM provides the capability; this use case
 * decides when to check).
 */

@Service
public class JulyConfigUseCase {

    /**
     * JulyConfig repository.
     */
    private final JulyConfigRepository repository;

    /**
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * Platform export use case.
     */
    private final ExportUseCase exportUseCase;

    /**
     * Platform backup use case.
     */
    private final BackupUseCase backupUseCase;

    /**
     * Create the use case.
     *
     * @param repository         july config repository
     * @param authorizationPort  authorization port
     * @param exportUseCase      export use case
     * @param backupUseCase      backup use case
     */
    public JulyConfigUseCase(JulyConfigRepository repository, AuthorizationPort authorizationPort,
            ExportUseCase exportUseCase, BackupUseCase backupUseCase) {
        this.repository = repository;
        this.authorizationPort = authorizationPort;
        this.exportUseCase = exportUseCase;
        this.backupUseCase = backupUseCase;
    }

    /**
     * Insert a new config entry.
     *
     * @param operatorId operator user id
     * @param code       config key, unique
     * @param data       config value
     * @param status     config status, null defaults to enabled
     * @param remark     remark, optional
     * @return new config id
     */
    @Transactional
    public String insert(String operatorId, String code, String data, String status, String remark) {
        authorizationPort.assertHas(operatorId, JulyConfigPermissionCodes011.INSERT);

        if (repository.findEnabledByCode(code) != null) {
            throw BusinessException.badRequest("config code already exists: " + code);
        }

        JulyConfig config = JulyConfig.create(EntityId.generate(), code, data, status, remark, AuditInfo.empty());
        repository.insert(config);

        return config.id().value();
    }

    /**
     * Update the value / status / remark of a config entry (code immutable).
     *
     * @param operatorId operator user id
     * @param id         config id
     * @param data       config value
     * @param status     config status, null keeps the stored one
     * @param remark     remark, optional
     * @return config id
     */
    @Transactional
    public String update(String operatorId, String id, String data, String status, String remark) {
        authorizationPort.assertHas(operatorId, JulyConfigPermissionCodes011.UPDATE);
        JulyConfig config = require(id);
        config.updateData(data, status, remark);
        repository.update(config);

        return config.id().value();
    }

    /**
     * Logic delete a config entry.
     *
     * @param operatorId operator user id
     * @param id         config id
     * @return deleted config id
     */
    @Transactional
    public String logicDelete(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyConfigPermissionCodes011.LOGIC_DELETE);

        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Find by primary key.
     *
     * @param operatorId operator user id
     * @param id         config id
     * @return aggregate
     */
    public JulyConfig getById(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyConfigPermissionCodes011.SELECT);
        return require(id);
    }

    /**
     * Program-facing read entry: the ENABLED config of a key, null when
     * missing or disabled. Readers query the store every time (no cache).
     * No HTTP / no permission check — internal program path.
     *
     * @param code config key
     * @return aggregate or null
     */
    public JulyConfig getByCode(String code) {
        return repository.findEnabledByCode(code);
    }

    /**
     * Page query with an optional keyword / status filter.
     *
     * @param operatorId operator user id
     * @param pageQuery  page query, null falls back to page 1 / size 10
     * @param keyword    code / data keyword, nullable
     * @param status     row status, nullable
     * @return page result
     */
    public PageResult011<JulyConfig> selectListByPage(String operatorId, PageQuery011 pageQuery, String keyword,
            String status) {
        authorizationPort.assertHas(operatorId, JulyConfigPermissionCodes011.SELECT);
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        List<JulyConfig> rows = repository.findPage(query.offset(), query.pageSize(), keyword, status);
        long total = repository.count(keyword, status);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Export all config rows (permission-gated; payload is export result only).
     *
     * @param operator authenticated operator
     * @return export result
     */
    public ExportResult export(Operator011 operator) {
        requireOperator(operator);
        authorizationPort.assertHas(operator.id(), JulyConfigPermissionCodes011.EXPORT);
        return exportUseCase.export(AuditObjectCodes011.JULY_CONFIG, operator);
    }

    /**
     * Backup all config rows to object storage (permission-gated). Returns the
     * storage object key only — never the config row payload.
     *
     * @param operator authenticated operator
     * @return storage object key
     */
    public String backup(Operator011 operator) {
        requireOperator(operator);
        authorizationPort.assertHas(operator.id(), JulyConfigPermissionCodes011.BACKUP);
        return backupUseCase.backup(AuditObjectCodes011.JULY_CONFIG, operator);
    }

    /**
     * Require an authenticated operator.
     *
     * @param operator operator
     */
    private void requireOperator(Operator011 operator) {
        if (operator == null || !operator.authenticated()) {
            throw BusinessException.unauthorized("not authenticated");
        }
    }

    /**
     * Require an existing config entry.
     *
     * @param id config id
     * @return aggregate
     */
    private JulyConfig require(String id) {
        JulyConfig config = repository.findById(id);

        if (config == null) {
            throw BusinessException.recordNotFound(id);
        }

        return config;
    }
}
