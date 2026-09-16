package com.klsjnh.infrastructure.system011.repository;

/*                JulyDictionaryItemRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary item repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItem;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItemRepository;

import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.system011.entity.JulyDictionaryItemPo;
import com.klsjnh.infrastructure.system011.mapper.JulyDictionaryItemMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

/**
 * Repository implementation for the JulyDictionaryItem child entity on the base
 * repository (july_dictionary_item). Uniqueness is composite
 * (pk_mt, item_code), enforced in the use case and by the DDL unique key.
 */

@Repository
public class JulyDictionaryItemRepositoryImpl
        extends BaseRepository<JulyDictionaryItemPo, JulyDictionaryItemMapper>
        implements JulyDictionaryItemRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyDictionaryItemRepositoryImpl(JulyDictionaryItemMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_dictionary_item";
    }

    /**
     * Business unique column name; blank — uniqueness is composite and handled
     * by the use case plus the DDL unique key.
     *
     * @return empty string
     */
    @Override
    protected String getBusinessColumn() {
        return "";
    }

    /**
     * Insert a new item.
     *
     * @param item entity
     */
    @Override
    public void insert(JulyDictionaryItem item) {
        insert(toPo(item));
    }

    /**
     * Update an existing item.
     *
     * @param item entity with id
     */
    @Override
    public void update(JulyDictionaryItem item) {
        update(toPo(item));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return entity or null
     */
    @Override
    public JulyDictionaryItem findById(String id) {
        JulyDictionaryItemPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find the ENABLED items of a dictionary, ordered by sort order.
     *
     * @param dictionaryId dictionary id
     * @return ordered entities, never null
     */
    @Override
    public List<JulyDictionaryItem> findByMaster(String dictionaryId) {
        return findAllByMaster(dictionaryId, Status011.ENABLED.getCode());
    }

    /**
     * Find the items of a dictionary (management view, optional status filter).
     *
     * @param dictionaryId dictionary id
     * @param status       optional status filter, null for all
     * @return ordered entities, never null
     */
    @Override
    public List<JulyDictionaryItem> findAllByMaster(String dictionaryId, String status) {
        QueryWrapper<JulyDictionaryItemPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", dictionaryId);

        if (!StringUtil011.isBlank(status)) {
            wrapper.eq("status", status);
        }

        wrapper.orderByAsc("sort_order").orderByAsc("id");

        return mapper.selectList(wrapper).stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Find one item by dictionary id and item code.
     *
     * @param dictionaryId dictionary id
     * @param itemCode     item code
     * @return entity or null
     */
    @Override
    public JulyDictionaryItem findByMasterAndCode(String dictionaryId, String itemCode) {
        QueryWrapper<JulyDictionaryItemPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", dictionaryId)
                .eq("item_code", itemCode)
                .last("LIMIT 1");

        JulyDictionaryItemPo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Count the alive items of a dictionary.
     *
     * @param dictionaryId dictionary id
     * @return alive item count
     */
    @Override
    public long countByMaster(String dictionaryId) {
        QueryWrapper<JulyDictionaryItemPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", dictionaryId);

        return mapper.selectCount(wrapper);
    }

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        JulyDictionaryItemPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /**
     * Map the entity to a PO.
     *
     * @param item entity
     * @return PO
     */
    private JulyDictionaryItemPo toPo(JulyDictionaryItem item) {
        JulyDictionaryItemPo po = new JulyDictionaryItemPo();
        po.setId(item.id().value());
        po.setPkMt(item.pkMt());
        po.setSortOrder(item.sortOrder());
        po.setItemCode(item.itemCode());
        po.setItemLabel(item.itemLabel());
        po.setStatus(item.status());
        po.setRemark(item.remark());

        return po;
    }

    /**
     * Map a PO to the entity.
     *
     * @param po PO
     * @return entity
     */
    private JulyDictionaryItem toAggregate(JulyDictionaryItemPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyDictionaryItem(EntityId.of(po.getId()), po.getPkMt(), po.getSortOrder(), po.getItemCode(),
                po.getItemLabel(), po.getStatus(), po.getRemark(), audit);
    }
}
