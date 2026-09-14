package com.klsjnh.demo11.infrastructure.persistence.repository;

/*                Demo011RepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  demo 011 repository impl class
 *
 */

import com.klsjnh.demo11.domain.Demo011;
import com.klsjnh.demo11.domain.Demo011Repository;
import com.klsjnh.demo11.infrastructure.persistence.entity.Demo011Po;
import com.klsjnh.demo11.infrastructure.persistence.mapper.Demo011Mapper;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository implementation for the Demo011 aggregate on the base repository
 * (BaseRepository) — the reference implementation for third parties.
 */

@Repository
public class Demo011RepositoryImpl
        extends BaseRepository<Demo011Po, Demo011Mapper>
        implements Demo011Repository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public Demo011RepositoryImpl(Demo011Mapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_demo011";
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
    protected Object getBusinessValue(Demo011Po entity) {
        return entity.getCode();
    }

    /**
     * Insert a new aggregate.
     *
     * @param demo aggregate
     */
    @Override
    public void insert(Demo011 demo) {
        insert(toPo(demo));
    }

    /**
     * Update an existing aggregate.
     *
     * @param demo aggregate with id
     */
    @Override
    public void update(Demo011 demo) {
        update(toPo(demo));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public Demo011 findById(String id) {
        Demo011Po po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by the unique code.
     *
     * @param code demo code
     * @return aggregate or null
     */
    @Override
    public Demo011 findByCode(String code) {
        Demo011Po po = getByBusinessValue(code);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        Demo011Po po = getById(id);

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
     * @param keyword  code / name keyword, nullable
     * @return page rows
     */
    @Override
    public List<Demo011> findPage(int offset, int pageSize, String keyword) {
        int current = offset / pageSize + 1;
        Page<Demo011Po> page = Page.of(current, pageSize);

        return mapper.selectPage(page, keywordWrapper(keyword)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Count with the same filter as findPage.
     *
     * @param keyword code / name keyword, nullable
     * @return total row count
     */
    @Override
    public long count(String keyword) {
        return mapper.selectCount(keywordWrapper(keyword));
    }

    /**
     * Keyword filter wrapper shared by findPage and count.
     *
     * @param keyword code / name keyword, nullable
     * @return query wrapper
     */
    private QueryWrapper<Demo011Po> keywordWrapper(String keyword) {
        QueryWrapper<Demo011Po> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("code", keyword).or().like("name", keyword);
        }

        wrapper.orderByDesc("create_time").orderByAsc("id");

        return wrapper;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param demo aggregate
     * @return PO
     */
    private Demo011Po toPo(Demo011 demo) {
        Demo011Po po = new Demo011Po();
        po.setId(demo.id().value());
        po.setCode(demo.code());
        po.setName(demo.name());
        po.setStatus(demo.status());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private Demo011 toAggregate(Demo011Po po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new Demo011(EntityId.of(po.getId()), po.getCode(), po.getName(), po.getStatus(), audit);
    }
}
