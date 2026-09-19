package com.klsjnh.infrastructure.system011.dictionary.repository;

/*                JulyDictionaryRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.dictionary.JulyDictionary;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryQuerySpec;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryRepository;

import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.system011.dictionary.entity.JulyDictionaryPo;
import com.klsjnh.infrastructure.system011.dictionary.mapper.JulyDictionaryMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyDictionary aggregate on the base
 * repository (july_dictionary, business unique column dictionary_code).
 */

@Repository
public class JulyDictionaryRepositoryImpl
        extends BaseRepository<JulyDictionaryPo, JulyDictionaryMapper>
        implements JulyDictionaryRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyDictionaryRepositoryImpl(JulyDictionaryMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_dictionary";
    }

    /**
     * Business unique column name.
     *
     * @return column name
     */
    @Override
    protected String getBusinessColumn() {
        return "dictionary_code";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyDictionaryPo entity) {
        return entity.getDictionaryCode();
    }

    /**
     * Duplicate message for the unique dictionaryCode.
     *
     * @return message
     */
    @Override
    protected String duplicateMessage() {
        return "dictionary code already exists";
    }

    /**
     * Insert a new aggregate.
     *
     * @param dictionary aggregate
     */
    @Override
    public void insert(JulyDictionary dictionary) {
        insert(toPo(dictionary));
    }

    /**
     * Update an existing aggregate.
     *
     * @param dictionary aggregate with id
     */
    @Override
    public void update(JulyDictionary dictionary) {
        update(toPo(dictionary));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyDictionary findById(String id) {
        JulyDictionaryPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by dictionary code, enabled or not (management read entry).
     *
     * @param dictionaryCode dictionary code
     * @return aggregate or null
     */
    @Override
    public JulyDictionary findByCode(String dictionaryCode) {
        JulyDictionaryPo po = getByBusinessValue(dictionaryCode);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by dictionary code, ENABLED only (program read entry).
     *
     * @param dictionaryCode dictionary code
     * @return aggregate or null
     */
    @Override
    public JulyDictionary findEnabledByCode(String dictionaryCode) {
        QueryWrapper<JulyDictionaryPo> wrapper = new QueryWrapper<>();
        wrapper.eq("dictionary_code", dictionaryCode)
                .eq("status", Status011.ENABLED.getCode())
                .last("LIMIT 1");

        JulyDictionaryPo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Every ENABLED dictionary, ordered by sort order.
     *
     * @return enabled aggregates, never null
     */
    @Override
    public List<JulyDictionary> findAllEnabled() {
        QueryWrapper<JulyDictionaryPo> wrapper = new QueryWrapper<>();
        wrapper.eq("status", Status011.ENABLED.getCode())
                .orderByAsc("sort_order")
                .orderByAsc("id");

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
        JulyDictionaryPo po = getById(id);

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
    public List<JulyDictionary> findPage(int offset, int pageSize, JulyDictionaryQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyDictionaryPo> page = Page.of(current, pageSize);

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
    public long count(JulyDictionaryQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyDictionaryPo> specWrapper(JulyDictionaryQuerySpec spec) {
        QueryWrapper<JulyDictionaryPo> wrapper = new QueryWrapper<>();
        JulyDictionaryQuerySpec query = spec == null ? new JulyDictionaryQuerySpec(null, null) : spec;

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("dictionary_code", keyword)
                    .or().like("dictionary_name", keyword));
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
     * @param dictionary aggregate
     * @return PO
     */
    private JulyDictionaryPo toPo(JulyDictionary dictionary) {
        JulyDictionaryPo po = new JulyDictionaryPo();
        po.setId(dictionary.id().value());
        po.setSortOrder(dictionary.sortOrder());
        po.setDictionaryCode(dictionary.dictionaryCode());
        po.setDictionaryName(dictionary.dictionaryName());
        po.setStatus(dictionary.status());
        po.setRemark(dictionary.remark());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyDictionary toAggregate(JulyDictionaryPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyDictionary(EntityId.of(po.getId()), po.getDictionaryCode(), po.getSortOrder(),
                po.getDictionaryName(), po.getStatus(), po.getRemark(), audit);
    }
}
