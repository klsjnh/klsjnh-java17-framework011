package com.klsjnh.infrastructure.dataservice011.repository;

/*                JulyBusinessModelingRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling repository impl class
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.dataservice011.JulyBusinessModeling;
import com.klsjnh.domain.dataservice011.JulyBusinessModelingQuerySpec;
import com.klsjnh.domain.dataservice011.JulyBusinessModelingRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.dataservice011.entity.JulyBusinessModelingPo;
import com.klsjnh.infrastructure.dataservice011.mapper.JulyBusinessModelingMapper;
import com.klsjnh.infrastructure.dataservice011.support.ModelDataCodec011;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyBusinessModeling aggregate on the base
 * repository (july_business_modeling, business unique column model_code). The
 * model_data JSON column is translated by {@link ModelDataCodec011}.
 */

@Repository
public class JulyBusinessModelingRepositoryImpl
        extends BaseRepository<JulyBusinessModelingPo, JulyBusinessModelingMapper>
        implements JulyBusinessModelingRepository {

    /**
     * Model data codec.
     */
    private final ModelDataCodec011 codec;

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     * @param codec        model data codec
     */
    public JulyBusinessModelingRepositoryImpl(JulyBusinessModelingMapper mapper, CommonMapper commonMapper,
            ModelDataCodec011 codec) {
        super(mapper, commonMapper);
        this.codec = codec;
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_business_modeling";
    }

    /**
     * Business unique column name.
     *
     * @return column name
     */
    @Override
    protected String getBusinessColumn() {
        return "model_code";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyBusinessModelingPo entity) {
        return entity.getModelCode();
    }

    /**
     * Duplicate message for the unique modelCode.
     *
     * @return message
     */
    @Override
    protected String duplicateMessage() {
        return "modeling code already exists";
    }

    /**
     * Insert a new aggregate.
     *
     * @param modeling aggregate
     */
    @Override
    public void insert(JulyBusinessModeling modeling) {
        insert(toPo(modeling));
    }

    /**
     * Update an existing aggregate.
     *
     * @param modeling aggregate with id
     */
    @Override
    public void update(JulyBusinessModeling modeling) {
        update(toPo(modeling));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyBusinessModeling findById(String id) {
        JulyBusinessModelingPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by modeling code.
     *
     * @param modelCode modeling code
     * @return aggregate or null
     */
    @Override
    public JulyBusinessModeling findByCode(String modelCode) {
        JulyBusinessModelingPo po = getByBusinessValue(modelCode);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by low-code object name.
     *
     * @param objectName object name
     * @return aggregate or null
     */
    @Override
    public JulyBusinessModeling findByObjectName(String objectName) {
        QueryWrapper<JulyBusinessModelingPo> wrapper = new QueryWrapper<>();
        wrapper.eq("object_name", objectName)
                .last("LIMIT 1");

        JulyBusinessModelingPo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Count rows carrying an object name other than the given one.
     *
     * @param objectName object name
     * @param excludeId  primary key to exclude, nullable for none
     * @return matching row count
     */
    @Override
    public long countByObjectNameExcluding(String objectName, String excludeId) {
        QueryWrapper<JulyBusinessModelingPo> wrapper = new QueryWrapper<>();
        wrapper.eq("object_name", objectName);

        if (!StringUtil011.isBlank(excludeId)) {
            wrapper.ne("id", excludeId);
        }

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
        JulyBusinessModelingPo po = getById(id);

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
    public List<JulyBusinessModeling> findPage(int offset, int pageSize, JulyBusinessModelingQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyBusinessModelingPo> page = Page.of(current, pageSize);

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
    public long count(JulyBusinessModelingQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyBusinessModelingPo> specWrapper(JulyBusinessModelingQuerySpec spec) {
        QueryWrapper<JulyBusinessModelingPo> wrapper = new QueryWrapper<>();
        JulyBusinessModelingQuerySpec query = spec == null ? new JulyBusinessModelingQuerySpec(null, null, null) : spec;

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("model_code", keyword)
                    .or().like("model_name", keyword)
                    .or().like("object_name", keyword));
        }

        if (query.hasDataSourceCode()) {
            wrapper.eq("data_source_code", query.dataSourceCode());
        }

        if (query.hasStatus()) {
            wrapper.eq("status", query.status());
        }

        wrapper.orderByDesc("create_time").orderByDesc("id");

        return wrapper;
    }

    /**
     * Map the aggregate to a PO (encoding the description JSON).
     *
     * @param modeling aggregate
     * @return PO
     */
    private JulyBusinessModelingPo toPo(JulyBusinessModeling modeling) {
        JulyBusinessModelingPo po = new JulyBusinessModelingPo();
        po.setId(modeling.id().value());
        po.setModelCode(modeling.modelCode());
        po.setModelName(modeling.modelName());
        po.setObjectName(modeling.objectName());
        po.setDataSourceCode(modeling.dataSourceCode());
        po.setSqlContent(modeling.sqlContent());
        po.setModelData(codec.encode(modeling.content()));
        po.setRemark(modeling.remark());
        po.setStatus(modeling.status());

        return po;
    }

    /**
     * Map a PO to the aggregate (decoding the description JSON).
     *
     * @param po PO
     * @return aggregate
     */
    private JulyBusinessModeling toAggregate(JulyBusinessModelingPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyBusinessModeling(EntityId.of(po.getId()), po.getModelCode(), po.getModelName(),
                codec.decode(po.getModelData(), po.getObjectName()), po.getDataSourceCode(), po.getSqlContent(),
                po.getRemark(), po.getStatus(), audit);
    }
}
