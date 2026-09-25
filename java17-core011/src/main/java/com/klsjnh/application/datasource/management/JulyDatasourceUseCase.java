package com.klsjnh.application.datasource.management;

/*                JulyDatasourceUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource use case class
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.StringUtil011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.domain.datasource.management.JulyDatasource;
import com.klsjnh.domain.datasource.management.JulyDatasourceQuerySpec;
import com.klsjnh.domain.datasource.management.JulyDatasourceRepository;
import com.klsjnh.domain.datasource.kernel.ConnectionInfo;
import com.klsjnh.domain.datasource.kernel.DataSourceProbePort;
import com.klsjnh.domain.datasource.kernel.DynamicDataSourceRegistryPort;
import com.klsjnh.domain.datasource.kernel.ReloadResult;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * JulyDatasource use cases: admin CRUD over the runtime datasource registry,
 * plus the connectivity probe and the registry reload.
 * <p>
 * The registry reload is the bridge between the table and the runtime: every
 * write case that can change routing (insert / update / logic delete) refreshes
 * the registry right away, so a saved datasource is routable at once and a
 * deleted one stops being routable at once. A probe never touches the registry.
 * </p>
 */

@Service
public class JulyDatasourceUseCase {

    /**
     * JulyDatasource repository.
     */
    private final JulyDatasourceRepository repository;

    /**
     * Dynamic datasource registry (declaration + reload).
     */
    private final DynamicDataSourceRegistryPort registry;

    /**
     * Connectivity probe (registry free).
     */
    private final DataSourceProbePort probePort;

    /**
     * Create the use case.
     *
     * @param repository july datasource repository
     * @param registry   dynamic datasource registry
     * @param probePort  connectivity probe
     */
    public JulyDatasourceUseCase(JulyDatasourceRepository repository, DynamicDataSourceRegistryPort registry,
            DataSourceProbePort probePort) {
        this.repository = repository;
        this.registry = registry;
        this.probePort = probePort;
    }

    /**
     * Insert a new datasource and refresh the registry.
     *
     * @param dsCode      datasource code, unique, immutable
     * @param sortOrder   manual sort order, null falls back to the default
     * @param dsName      datasource name
     * @param dbType      database type code
     * @param jdbcUrl     jdbc url
     * @param schemaName  schema name, optional
     * @param username    login user, optional
     * @param password    login password, optional
     * @param driverClass jdbc driver class, optional
     * @param remark      remark, optional
     * @return new datasource id
     */
    @Transactional
    public String insert(String dsCode, Integer sortOrder, String dsName, String dbType, String jdbcUrl,
            String schemaName, String username, String password, String driverClass, String remark) {
        requireDbType(dbType);

        if (repository.findByCode(dsCode) != null) {
            throw BusinessException.badRequest("datasource code already exists: " + dsCode);
        }

        JulyDatasource datasource = newAggregate(dsCode, sortOrder, dsName, dbType, jdbcUrl, schemaName, username,
                password, driverClass, remark);
        repository.insert(datasource);
        reloadRegistry();

        return datasource.id().value();
    }

    /**
     * Update a datasource and refresh the registry. A blank password keeps the
     * stored one (the web layer never echoes it back).
     *
     * @param id          datasource id
     * @param dsName      datasource name
     * @param sortOrder   manual sort order, null keeps the stored one
     * @param dbType      database type code
     * @param jdbcUrl     jdbc url
     * @param schemaName  schema name, optional
     * @param username    login user, optional
     * @param password    login password, optional; blank keeps the stored one
     * @param driverClass jdbc driver class, optional
     * @param remark      remark, optional
     * @return datasource id
     */
    @Transactional
    public String update(String id, String dsName, Integer sortOrder, String dbType, String jdbcUrl, String schemaName,
            String username, String password, String driverClass, String remark) {
        JulyDatasource datasource = require(id);
        requireDbType(dbType);
        applyUpdate(datasource, dsName, sortOrder, dbType, jdbcUrl, schemaName, username, password, driverClass, remark);
        repository.update(datasource);
        reloadRegistry();

        return datasource.id().value();
    }

    /**
     * Logic delete a datasource and refresh the registry.
     *
     * @param id datasource id
     * @return deleted datasource id
     */
    @Transactional
    public String logicDelete(String id) {
        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        reloadRegistry();

        return id;
    }

    /**
     * Batch logic delete, all-or-nothing: a missing id fails the whole batch
     * (404) so the transaction rolls back; only then the registry is refreshed
     * once for the batch.
     *
     * @param ids datasource ids
     * @return batch delete summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(List<String> ids) {
        List<String> normalized = normalize(ids);

        if (normalized.isEmpty()) {
            throw BusinessException.badRequest("batch logic delete: ids is required");
        }

        repository.logicDeleteByIds(normalized);
        reloadRegistry();

        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        result.setTotal(normalized.size());
        result.setSuccess(normalized.size());
        result.setFailed(0);

        return result;
    }

    /**
     * Find by primary key.
     *
     * @param id datasource id
     * @return aggregate
     */
    public JulyDatasource getById(String id) {
        return require(id);
    }

    /**
     * Page query on the management view.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param spec      query condition, null means no filter
     * @return page result
     */
    public PageResult011<JulyDatasource> selectListByPage(PageQuery011 pageQuery, JulyDatasourceQuerySpec spec) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        JulyDatasourceQuerySpec condition = spec == null ? new JulyDatasourceQuerySpec(null, null) : spec;
        List<JulyDatasource> rows = repository.findPage(query.offset(), query.pageSize(), condition);
        long total = repository.count(condition);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Probe an UNSAVED candidate config (the form's "test connection" button
     * before saving) — never registers anything, so the probe cannot pollute
     * the runtime registry.
     *
     * @param dsCode      datasource code, display only in the log
     * @param dbType      database type code
     * @param jdbcUrl     jdbc url
     * @param username    login user, optional
     * @param password    login password, optional
     * @param driverClass jdbc driver class, optional
     * @return probe result
     */
    public DataSourceProbePort.ProbeResult testDraft(String dsCode, String dbType, String jdbcUrl, String username,
            String password, String driverClass) {
        requireDbType(dbType);
        requireJdbcUrl(jdbcUrl);

        String code = StringUtil011.isBlank(dsCode) ? "draft" : dsCode.trim();
        ConnectionInfo info = new ConnectionInfo(code, code, dbType, jdbcUrl, username, password, null, driverClass,
                null);

        return probePort.probe(info);
    }

    /**
     * Probe a SAVED datasource (the list's "retest" button): the stored
     * password is used as is, which is the only way to verify a datasource
     * without the caller ever holding the password. A non-blank password
     * overrides the stored one.
     *
     * @param id       datasource id
     * @param password password override, blank uses the stored one
     * @return probe result
     */
    public DataSourceProbePort.ProbeResult testSaved(String id, String password) {
        JulyDatasource datasource = require(id);

        if (StringUtil011.isBlank(password)) {
            return probePort.probe(toConnectionInfo(datasource));
        }

        ConnectionInfo info = new ConnectionInfo(datasource.dsCode(), datasource.dsName(), datasource.dbType(),
                datasource.jdbcUrl(), datasource.username(), password, datasource.schemaName(),
                datasource.driverClass(), datasource.poolConfig());

        return probePort.probe(info);
    }

    /**
     * Refresh the registry from the table (the table-driven reload entry).
     *
     * @return reconciliation summary
     */
    public ReloadResult reloadRegistry() {
        List<JulyDatasource> enabled = repository.findAllEnabled();
        List<ConnectionInfo> infos = new ArrayList<>();

        for (JulyDatasource datasource : enabled) {
            infos.add(toConnectionInfo(datasource));
        }

        return registry.reloadAll(infos);
    }

    /**
     * Apply an update on the aggregate, translating domain validation failures
     * into 400 responses.
     *
     * @param datasource  aggregate
     * @param dsName      datasource name
     * @param sortOrder   manual sort order
     * @param dbType      database type code
     * @param jdbcUrl     jdbc url
     * @param schemaName  schema name
     * @param username    login user
     * @param password    login password
     * @param driverClass jdbc driver class
     * @param remark      remark
     */
    private void applyUpdate(JulyDatasource datasource, String dsName, Integer sortOrder, String dbType, String jdbcUrl,
            String schemaName, String username, String password, String driverClass, String remark) {
        try {
            datasource.update(dsName, sortOrder, dbType, jdbcUrl, schemaName, username, password, driverClass, remark);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Map the aggregate to the registry connection info.
     *
     * @param datasource aggregate
     * @return connection info
     */
    private ConnectionInfo toConnectionInfo(JulyDatasource datasource) {
        return new ConnectionInfo(datasource.dsCode(), datasource.dsName(), datasource.dbType(),
                datasource.jdbcUrl(), datasource.username(), datasource.password(), datasource.schemaName(),
                datasource.driverClass(), datasource.poolConfig());
    }

    /**
     * Build a new aggregate from raw fields, translating domain validation
     * failures into 400 responses.
     *
     * @param dsCode      datasource code
     * @param sortOrder   manual sort order
     * @param dsName      datasource name
     * @param dbType      database type code
     * @param jdbcUrl     jdbc url
     * @param schemaName  schema name
     * @param username    login user
     * @param password    login password
     * @param driverClass jdbc driver class
     * @param remark      remark
     * @return new aggregate
     */
    private JulyDatasource newAggregate(String dsCode, Integer sortOrder, String dsName, String dbType, String jdbcUrl,
            String schemaName, String username, String password, String driverClass, String remark) {
        try {
            return JulyDatasource.create(EntityId.generate(), dsCode, sortOrder, dsName, dbType, jdbcUrl, schemaName,
                    username, password, driverClass, remark, AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Require an existing datasource.
     *
     * @param id datasource id
     * @return aggregate
     */
    private JulyDatasource require(String id) {
        JulyDatasource datasource = repository.findById(id);

        if (datasource == null) {
            throw BusinessException.recordNotFound(id);
        }

        return datasource;
    }

    /**
     * Reject an unknown database type with a 400.
     *
     * @param dbType database type code
     */
    private void requireDbType(String dbType) {
        if (StringUtil011.isMissing(dbType, 20)) {
            throw BusinessException.badRequest("database type is required");
        }
    }

    /**
     * Reject a missing / malformed jdbc url with a 400.
     *
     * @param jdbcUrl jdbc url
     */
    private void requireJdbcUrl(String jdbcUrl) {
        if (StringUtil011.isBlank(jdbcUrl) || !jdbcUrl.startsWith("jdbc:")) {
            throw BusinessException.badRequest("jdbc url is required and must start with jdbc:");
        }
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
