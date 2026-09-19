package com.klsjnh.infrastructure.system011.config.repository;

/*                JulyConfigRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.system011.config.JulyConfig;
import com.klsjnh.domain.system011.config.JulyConfigRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.system011.config.entity.JulyConfigPo;
import com.klsjnh.infrastructure.system011.config.mapper.JulyConfigMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyConfig aggregate on the base
 * repository. Readers query every time (no cache — user decision).
 */

@Repository
public class JulyConfigRepositoryImpl
        extends BaseRepository<JulyConfigPo, JulyConfigMapper>
        implements JulyConfigRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyConfigRepositoryImpl(JulyConfigMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_config";
    }

    /**
     * Business unique column name.
     *
     * @return column name
     */
    @Override
    protected String getBusinessColumn() {
        return "code";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyConfigPo entity) {
        return entity.getCode();
    }

    /**
     * Insert a new aggregate.
     *
     * @param config aggregate
     */
    @Override
    public void insert(JulyConfig config) {
        insert(toPo(config));
    }

    /**
     * Update an existing aggregate.
     *
     * @param config aggregate with id
     */
    @Override
    public void update(JulyConfig config) {
        update(toPo(config));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyConfig findById(String id) {
        JulyConfigPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find an ENABLED config by its unique key (disabled / missing → null) —
     * the program-facing read entry.
     *
     * @param code config key
     * @return aggregate or null
     */
    @Override
    public JulyConfig findEnabledByCode(String code) {
        JulyConfigPo po = getByBusinessValue(code);

        if (po == null || !Status011.ENABLED.getCode().equals(po.getStatus())) {
            return null;
        }

        return toAggregate(po);
    }

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        JulyConfigPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /**
     * Offset based page query with an optional keyword filter.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param keyword  code / data keyword, nullable
     * @return page rows
     */
    @Override
    public List<JulyConfig> findPage(int offset, int pageSize, String keyword) {
        int current = offset / pageSize + 1;
        Page<JulyConfigPo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, keywordWrapper(keyword)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Count with the same filter as findPage.
     *
     * @param keyword code / data keyword, nullable
     * @return total row count
     */
    @Override
    public long count(String keyword) {
        return mapper.selectCount(keywordWrapper(keyword));
    }

    /**
     * Keyword filter wrapper shared by findPage and count.
     *
     * @param keyword code / data keyword, nullable
     * @return query wrapper
     */
    private QueryWrapper<JulyConfigPo> keywordWrapper(String keyword) {
        QueryWrapper<JulyConfigPo> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("code", keyword).or().like("data", keyword);
        }

        wrapper.orderByDesc("create_time").orderByAsc("id");

        return wrapper;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param config aggregate
     * @return PO
     */
    private JulyConfigPo toPo(JulyConfig config) {
        JulyConfigPo po = new JulyConfigPo();
        po.setId(config.id().value());
        po.setCode(config.code());
        po.setData(config.data());
        po.setStatus(config.status());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyConfig toAggregate(JulyConfigPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyConfig(EntityId.of(po.getId()), po.getCode(), po.getData(), po.getStatus(), audit);
    }
}
