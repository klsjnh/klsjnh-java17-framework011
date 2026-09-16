package com.klsjnh.infrastructure.dataservice011.repository;

/*                JulyDatasourceRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.dataservice011.JulyDatasource;
import com.klsjnh.domain.dataservice011.JulyDatasourceQuerySpec;
import com.klsjnh.domain.dataservice011.JulyDatasourceRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.dataservice011.entity.JulyDatasourcePo;
import com.klsjnh.infrastructure.dataservice011.mapper.JulyDatasourceMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.persistence.sql.QuotedLiteral;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyDatasource aggregate on the base
 * repository. {@link #findAllEnabled()} feeds the dynamic datasource registry
 * reload; the paged queries serve the management view and keep disabled rows.
 */

@Repository
public class JulyDatasourceRepositoryImpl
        extends BaseRepository<JulyDatasourcePo, JulyDatasourceMapper>
        implements JulyDatasourceRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyDatasourceRepositoryImpl(JulyDatasourceMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_datasource";
    }

    /**
     * Business unique column name.
     *
     * @return column name
     */
    @Override
    protected String getBusinessColumn() {
        return "ds_code";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyDatasourcePo entity) {
        return entity.getDsCode();
    }

    /**
     * Duplicate message for the unique dsCode.
     *
     * @return message
     */
    @Override
    protected String duplicateMessage() {
        return "datasource code already exists";
    }

    /**
     * Insert a new aggregate.
     *
     * @param datasource aggregate
     */
    @Override
    public void insert(JulyDatasource datasource) {
        insert(toPo(datasource));
    }

    /**
     * Update an existing aggregate.
     *
     * @param datasource aggregate with id
     */
    @Override
    public void update(JulyDatasource datasource) {
        update(toPo(datasource));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyDatasource findById(String id) {
        JulyDatasourcePo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by datasource code, enabled or not (management read entry).
     *
     * @param dsCode datasource code
     * @return aggregate or null
     */
    @Override
    public JulyDatasource findByCode(String dsCode) {
        JulyDatasourcePo po = getByBusinessValue(dsCode);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Whether a dsCode exists including logic-deleted rows (seed idempotency).
     * <p>
     * Goes through {@link CommonMapper#countBy} on purpose: the entity query
     * path always carries the logic-delete filter, which would make a deleted
     * row invisible and let the yaml seed resurrect it on the next restart.
     * The dsCode is bound through a QuotedLiteral, never spliced raw.
     * </p>
     *
     * @param dsCode datasource code
     * @return true when a row exists under this dsCode, deleted or not
     */
    @Override
    public boolean existsIncludingDeleted(String dsCode) {
        if (StringUtil011.isBlank(dsCode)) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE " + getBusinessColumn() + " = "
                + QuotedLiteral.of(dsCode);

        return commonMapper.countBy(sql) > 0;
    }

    /**
     * Runtime read entry: every ENABLED datasource, ordered by dsCode — the
     * snapshot the dynamic datasource registry reloads from.
     *
     * @return enabled aggregates, never null
     */
    @Override
    public List<JulyDatasource> findAllEnabled() {
        QueryWrapper<JulyDatasourcePo> wrapper = new QueryWrapper<>();
        wrapper.eq("status", Status011.ENABLED.getCode())
                .orderByAsc("ds_code");

        return mapper.selectList(wrapper).stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        JulyDatasourcePo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /**
     * Offset based page query on the management view.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows
     */
    @Override
    public List<JulyDatasource> findPage(int offset, int pageSize, JulyDatasourceQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyDatasourcePo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, specWrapper(spec)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    @Override
    public long count(JulyDatasourceQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyDatasourcePo> specWrapper(JulyDatasourceQuerySpec spec) {
        QueryWrapper<JulyDatasourcePo> wrapper = new QueryWrapper<>();
        JulyDatasourceQuerySpec query = spec == null ? new JulyDatasourceQuerySpec(null, null) : spec;

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("ds_code", keyword)
                    .or().like("ds_name", keyword)
                    .or().like("jdbc_url", keyword));
        }

        if (query.hasStatus()) {
            wrapper.eq("status", query.status());
        }

        wrapper.orderByAsc("sort_order").orderByAsc("id");

        return wrapper;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param datasource aggregate
     * @return PO
     */
    private JulyDatasourcePo toPo(JulyDatasource datasource) {
        JulyDatasourcePo po = new JulyDatasourcePo();
        po.setId(datasource.id().value());
        po.setDsCode(datasource.dsCode());
        po.setSortOrder(datasource.sortOrder());
        po.setDsName(datasource.dsName());
        po.setDbType(datasource.dbType());
        po.setJdbcUrl(datasource.jdbcUrl());
        po.setSchemaName(datasource.schemaName());
        po.setUsername(datasource.username());
        po.setPassword(datasource.password());
        po.setDriverClass(datasource.driverClass());
        po.setPoolConfig(datasource.poolConfig());
        po.setRemark(datasource.remark());
        po.setStatus(datasource.status());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyDatasource toAggregate(JulyDatasourcePo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyDatasource(EntityId.of(po.getId()), po.getDsCode(), po.getSortOrder(), po.getDsName(),
                po.getDbType(), po.getJdbcUrl(), po.getSchemaName(), po.getUsername(), po.getPassword(),
                po.getDriverClass(), po.getPoolConfig(), po.getRemark(), po.getStatus(), audit);
    }
}
