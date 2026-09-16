package com.klsjnh.infrastructure.lowcode011.repository;

/*                JulyMetadataRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata repository impl class
 *
 */

import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataDisplay;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataQuerySpec;
import com.klsjnh.domain.lowcode011.JulyMetadataRepository;
import com.klsjnh.domain.lowcode011.JulyMetadataService;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.lowcode011.entity.JulyMetadataDisplayPo;
import com.klsjnh.infrastructure.lowcode011.entity.JulyMetadataFieldPo;
import com.klsjnh.infrastructure.lowcode011.entity.JulyMetadataPo;
import com.klsjnh.infrastructure.lowcode011.entity.JulyMetadataServicePo;
import com.klsjnh.infrastructure.lowcode011.mapper.JulyMetadataDisplayMapper;
import com.klsjnh.infrastructure.lowcode011.mapper.JulyMetadataFieldMapper;
import com.klsjnh.infrastructure.lowcode011.mapper.JulyMetadataMapper;
import com.klsjnh.infrastructure.lowcode011.mapper.JulyMetadataServiceMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for the JulyMetadata aggregate on the base
 * repository: master CRUD plus cascade (replace) of the three child
 * collections.
 */

@Repository
public class JulyMetadataRepositoryImpl extends BaseRepository<JulyMetadataPo, JulyMetadataMapper>
        implements JulyMetadataRepository {

    /**
     * Field mapper.
     */
    private final JulyMetadataFieldMapper fieldMapper;

    /**
     * Display mapper.
     */
    private final JulyMetadataDisplayMapper displayMapper;

    /**
     * Service mapper.
     */
    private final JulyMetadataServiceMapper serviceMapper;

    /**
     * Create the repository.
     *
     * @param mapper        master mapper
     * @param commonMapper  native sql mapper
     * @param fieldMapper   field mapper
     * @param displayMapper display mapper
     * @param serviceMapper service mapper
     */
    public JulyMetadataRepositoryImpl(JulyMetadataMapper mapper, CommonMapper commonMapper,
            JulyMetadataFieldMapper fieldMapper, JulyMetadataDisplayMapper displayMapper,
            JulyMetadataServiceMapper serviceMapper) {
        super(mapper, commonMapper);
        this.fieldMapper = fieldMapper;
        this.displayMapper = displayMapper;
        this.serviceMapper = serviceMapper;
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_metadata";
    }

    /**
     * Business unique column name.
     *
     * @return column name
     */
    @Override
    protected String getBusinessColumn() {
        return "object_name";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyMetadataPo entity) {
        return entity.getObjectName();
    }

    /**
     * Duplicate message for the unique objectName.
     *
     * @return message
     */
    @Override
    protected String duplicateMessage() {
        return "object name already exists";
    }

    /**
     * Insert the aggregate and its children.
     *
     * @param metadata aggregate
     */
    @Override
    public void insert(JulyMetadata metadata) {
        JulyMetadataPo po = toPo(metadata);
        insert(po);
        insertChildren(po.getId(), metadata);
    }

    /**
     * Update the aggregate (children replaced).
     *
     * @param metadata aggregate with id
     */
    @Override
    public void update(JulyMetadata metadata) {
        JulyMetadataPo po = toPo(metadata);
        update(po);
        clearChildren(po.getId());
        insertChildren(po.getId(), metadata);
    }

    /**
     * Find by primary key together with its children.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyMetadata findById(String id) {
        JulyMetadataPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by object name together with its children.
     *
     * @param objectName object name
     * @return aggregate or null
     */
    @Override
    public JulyMetadata findByObjectName(String objectName) {
        JulyMetadataPo po = getByBusinessValue(objectName);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Logic delete the aggregate and cascade to its children.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        JulyMetadataPo po = getById(id);

        if (po == null) {
            return false;
        }

        clearChildren(id);
        logicDelete(po);

        return true;
    }

    /**
     * Offset based page query on the master.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows (without children)
     */
    @Override
    public List<JulyMetadata> findPage(int offset, int pageSize, JulyMetadataQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyMetadataPo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, specWrapper(spec)).getRecords().stream()
                .map(po -> toAggregate(po, false))
                .toList();
    }

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    @Override
    public long count(JulyMetadataQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Insert the three child collections under a master id.
     *
     * @param masterId master id
     * @param metadata aggregate
     */
    private void insertChildren(String masterId, JulyMetadata metadata) {
        for (JulyMetadataField field : metadata.fields()) {
            JulyMetadataFieldPo po = new JulyMetadataFieldPo();
            po.setPkMt(masterId);
            po.setSortOrder(field.sortOrder());
            po.setFieldCode(field.fieldCode());
            po.setFieldName(field.fieldName());
            po.setFieldType(field.fieldType());
            po.setFieldLength(field.fieldLength());
            po.setRequiredField(field.requiredField() ? "1" : "0");
            po.setDefaultValue(field.defaultValue());
            fieldMapper.insert(po);
        }

        for (JulyMetadataDisplay display : metadata.displays()) {
            JulyMetadataDisplayPo po = new JulyMetadataDisplayPo();
            po.setPkMt(masterId);
            po.setSortOrder(display.sortOrder());
            po.setDisplayCode(display.displayCode());
            po.setDisplayName(display.displayName());
            po.setAlign(display.align());
            po.setWidth(display.width());
            po.setComponentType(display.componentType());
            po.setDisplayType(display.displayType());
            po.setParam011(display.param011());
            displayMapper.insert(po);
        }

        for (JulyMetadataService service : metadata.services()) {
            JulyMetadataServicePo po = new JulyMetadataServicePo();
            po.setPkMt(masterId);
            po.setSortOrder(service.sortOrder());
            po.setServiceCode(service.serviceCode());
            po.setServiceName(service.serviceName());
            po.setServiceDescription(service.serviceDescription());
            po.setObjectType(service.objectType());
            po.setParamType(service.paramType());
            po.setServiceContent(service.serviceContent());
            po.setEnabled(service.enabled() ? "1" : "0");
            serviceMapper.insert(po);
        }
    }

    /**
     * Logic delete all children of a master.
     *
     * @param masterId master id
     */
    private void clearChildren(String masterId) {
        fieldMapper.deleteByMaster(masterId);
        displayMapper.deleteByMaster(masterId);
        serviceMapper.deleteByMaster(masterId);
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyMetadataPo> specWrapper(JulyMetadataQuerySpec spec) {
        QueryWrapper<JulyMetadataPo> wrapper = new QueryWrapper<>();
        JulyMetadataQuerySpec query = spec == null ? new JulyMetadataQuerySpec(null, null, null) : spec;

        if (query.hasKeyword()) {
            wrapper.like("object_name", query.keyword());
        }

        if (query.hasObjectType()) {
            wrapper.eq("object_type", query.objectType());
        }

        if (query.hasStatus()) {
            wrapper.eq("status", query.status());
        }

        wrapper.orderByAsc("sort_order").orderByAsc("id");

        return wrapper;
    }

    /**
     * Map the aggregate to a master PO.
     *
     * @param metadata aggregate
     * @return PO
     */
    private JulyMetadataPo toPo(JulyMetadata metadata) {
        JulyMetadataPo po = new JulyMetadataPo();
        po.setId(metadata.id() == null ? null : metadata.id().value());
        po.setSortOrder(metadata.sortOrder());
        po.setObjectName(metadata.objectName());
        po.setObjectType(metadata.objectType());
        po.setDescription(metadata.description());
        po.setBusinessField(metadata.businessField());
        po.setPackageName(metadata.packageName());
        po.setRouterPath(metadata.routerPath());
        po.setRemark(metadata.remark());
        po.setStatus(metadata.status());

        return po;
    }

    /**
     * Map a master PO to the aggregate with its children.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyMetadata toAggregate(JulyMetadataPo po) {
        return toAggregate(po, true);
    }

    /**
     * Map a master PO to the aggregate, optionally loading children.
     *
     * @param po          PO
     * @param withChildren whether to load children
     * @return aggregate
     */
    private JulyMetadata toAggregate(JulyMetadataPo po, boolean withChildren) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());
        JulyMetadata metadata = new JulyMetadata(EntityId.of(po.getId()), po.getObjectName(), po.getSortOrder(),
                po.getObjectType(), po.getDescription(), po.getBusinessField(), po.getPackageName(),
                po.getRouterPath(), po.getRemark(), po.getStatus(), audit);

        if (withChildren) {
            List<JulyMetadataField> fields = new ArrayList<>();
            for (JulyMetadataFieldPo child : fieldMapper.selectList(new QueryWrapper<JulyMetadataFieldPo>()
                    .eq("pk_mt", po.getId()).orderByAsc("sort_order").orderByAsc("id"))) {
                fields.add(new JulyMetadataField(child.getFieldCode(), child.getFieldName(), child.getFieldType(),
                        child.getFieldLength() == null ? 0 : child.getFieldLength(),
                        "1".equals(child.getRequiredField()), child.getDefaultValue(), child.getSortOrder()));
            }
            List<JulyMetadataDisplay> displays = new ArrayList<>();
            for (JulyMetadataDisplayPo child : displayMapper.selectList(new QueryWrapper<JulyMetadataDisplayPo>()
                    .eq("pk_mt", po.getId()).orderByAsc("sort_order").orderByAsc("id"))) {
                displays.add(new JulyMetadataDisplay(child.getDisplayCode(), child.getDisplayName(), child.getAlign(),
                        child.getWidth() == null ? 0 : child.getWidth(), child.getComponentType(),
                        child.getDisplayType(), child.getParam011(), child.getSortOrder()));
            }
            List<JulyMetadataService> services = new ArrayList<>();
            for (JulyMetadataServicePo child : serviceMapper.selectList(new QueryWrapper<JulyMetadataServicePo>()
                    .eq("pk_mt", po.getId()).orderByAsc("sort_order").orderByAsc("id"))) {
                services.add(new JulyMetadataService(child.getServiceCode(), child.getServiceName(),
                        child.getServiceDescription(), child.getObjectType(), child.getParamType(),
                        child.getServiceContent(), "1".equals(child.getEnabled()), child.getSortOrder()));
            }
            metadata.replaceChildren(fields, displays, services);
        }

        return metadata;
    }
}
