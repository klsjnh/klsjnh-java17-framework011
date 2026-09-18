package com.klsjnh.application.lowcode011;

/*                JulyMetadataDesignerUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata designer use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataRepository;
import com.klsjnh.domain.lowcode011.MetadataDdlGeneratorPort;
import com.klsjnh.domain.lowcode011.records.MetaDtoKey011;
import com.klsjnh.domain.lowcode011.records.MetadataContent;
import com.klsjnh.domain.lowcode011.records.MetadataContentCodec;
import com.klsjnh.domain.lowcode011.records.MetadataContentMapper;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Low-code designer use case (phase 1, read-only): list models, load a model as
 * the legacy MetaDTO shape, save the full MetaDTO back onto the one-master
 * three-children aggregate, and preview the publish DDL with the same generator
 * publish will use.
 */

@Service
public class JulyMetadataDesignerUseCase {

    /**
     * Physical table prefix for generated objects.
     */
    private static final String TABLE_PREFIX = "lc_";

    /**
     * Metadata CRUD use case (one master + three children).
     */
    private final JulyMetadataUseCase metadataUseCase;

    /**
     * Metadata repository (existence probe / id lookup).
     */
    private final JulyMetadataRepository repository;

    /**
     * DDL generator (shared with publish).
     */
    private final MetadataDdlGeneratorPort ddlGenerator;

    /**
     * Create the use case.
     *
     * @param metadataUseCase metadata CRUD use case
     * @param repository      metadata repository
     * @param ddlGenerator    DDL generator
     */
    public JulyMetadataDesignerUseCase(JulyMetadataUseCase metadataUseCase, JulyMetadataRepository repository,
            MetadataDdlGeneratorPort ddlGenerator) {
        this.metadataUseCase = metadataUseCase;
        this.repository = repository;
        this.ddlGenerator = ddlGenerator;
    }

    /**
     * List all models with a display status.
     *
     * @return model rows
     */
    public List<Map<String, Object>> listModels() {
        PageResult011<JulyMetadata> page = metadataUseCase.selectListByPage(new PageQuery011(1, 1000), null);
        List<Map<String, Object>> rows = new ArrayList<>();

        for (JulyMetadata item : page.rows()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("objectName", item.objectName());
            row.put("description", item.description());
            row.put("objectType", item.objectType());
            row.put("publishStatus", "draft");
            row.put("version", "");
            rows.add(row);
        }

        return rows;
    }

    /**
     * Load a model as the legacy MetaDTO shape.
     *
     * @param objectName object name
     * @return MetaDTO map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> load(String objectName) {
        JulyMetadata metadata = metadataUseCase.getByObjectName(objectName);
        Map<String, Object> dto = MetadataContentCodec.toMetaDto(MetadataContentMapper.from(metadata));
        Map<String, Object> metaData = (Map<String, Object>) dto.get(MetaDtoKey011.META_DATA);
        metaData.put(MetaDtoKey011.REMARK, metadata.remark());
        metaData.put(MetaDtoKey011.SORT_ORDER, metadata.sortOrder());
        metaData.put(MetaDtoKey011.PUBLISH_STATUS, "draft");
        metaData.put(MetaDtoKey011.VERSION, "");

        return dto;
    }

    /**
     * Save a model from the legacy MetaDTO shape (insert or update by object
     * name; the object name is immutable).
     *
     * @param body       MetaDTO body
     * @param sourceType source type (designer / probe)
     * @return object id
     */
    public String save(Map<String, Object> body, String sourceType) {
        if (body == null) {
            throw BusinessException.badRequest("metaData required");
        }

        MetadataContent content;

        try {
            content = MetadataContentCodec.fromMetaDto(body);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        Map<String, Object> metaData = asMap(body.get(MetaDtoKey011.META_DATA));
        String businessField = content.businessField();
        List<JulyMetadataField> fields = normalizeBusinessField(content.fields(), businessField);
        String remark = text(metaData == null ? null : metaData.get(MetaDtoKey011.REMARK));
        Integer sortOrder = integer(metaData == null ? null : metaData.get(MetaDtoKey011.SORT_ORDER));
        JulyMetadata existing = repository.findByObjectName(content.objectName());

        if (existing == null) {
            return metadataUseCase.insert(content.objectName(), sortOrder, content.objectType(), content.description(),
                    businessField, content.packageName(), content.routerPath(), remark, fields, content.displays(),
                    content.services());
        }

        return metadataUseCase.update(existing.id().value(), content.objectType(), content.description(), businessField,
                content.packageName(), content.routerPath(), sortOrder, null, remark, fields, content.displays(),
                content.services());
    }

    /**
     * Preview the publish DDL of a model.
     *
     * @param objectName object name
     * @return CREATE TABLE statement
     */
    public String previewDdl(String objectName) {
        JulyMetadata metadata = metadataUseCase.getByObjectName(objectName);

        try {
            return ddlGenerator.generateCreate(TABLE_PREFIX + metadata.objectName(), metadata.description(),
                    metadata.fields(), metadata.businessField());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }


    /**
     * Force the business field to the platform-fixed shape: string, length 33
     * (the business unique key, aligned with the surrogate id column).
     *
     * @param fields        parsed fields
     * @param businessField business field code, nullable
     * @return normalized fields
     */
    private List<JulyMetadataField> normalizeBusinessField(List<JulyMetadataField> fields, String businessField) {
        if (businessField == null || businessField.isBlank()) {
            return fields;
        }

        List<JulyMetadataField> normalized = new ArrayList<>();

        for (JulyMetadataField field : fields) {
            if (field.fieldCode() != null && field.fieldCode().equalsIgnoreCase(businessField)) {
                normalized.add(new JulyMetadataField(field.fieldCode(), field.fieldName(), "string", 33,
                        field.requiredField(), field.defaultValue(), field.sortOrder()));
            } else {
                normalized.add(field);
            }
        }

        return normalized;
    }

    /**
     * Cast a raw value to a map.
     *
     * @param value raw value
     * @return map or null
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    /**
     * Read a string value.
     *
     * @param value raw value
     * @return string or null
     */
    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Read a nullable integer value.
     *
     * @param value raw value
     * @return integer or null
     */
    private Integer integer(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }
}
