package com.klsjnh.web.lowcode011.converter;

/*                JulyBusinessModelingConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling converter class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.lowcode011.modeling.BusinessModelingProbeResult;
import com.klsjnh.domain.lowcode011.modeling.JulyBusinessModeling;
import com.klsjnh.domain.lowcode011.JulyMetadataField;

import com.klsjnh.web.lowcode011.vo.julybusinessmodeling.JulyBusinessModelingFieldVo011;
import com.klsjnh.web.lowcode011.vo.julybusinessmodeling.JulyBusinessModelingMetaVo011;
import com.klsjnh.web.lowcode011.vo.julybusinessmodeling.JulyBusinessModelingProbeResultVo011;
import com.klsjnh.web.lowcode011.vo.julybusinessmodeling.JulyBusinessModelingResultVo011;
import com.klsjnh.web.lowcode011.vo.julybusinessmodeling.JulyBusinessModelingVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converter between the JulyBusinessModeling aggregate / probe result and the
 * response VOs, plus the inbound field VO to domain field mapping.
 */

@Component
public class JulyBusinessModelingConverter {

    /**
     * Map the aggregate to the response VO (product + sqlContent).
     *
     * @param modeling aggregate
     * @return response VO
     */
    public JulyBusinessModelingVo011 toVo(JulyBusinessModeling modeling) {
        JulyBusinessModelingVo011 vo = new JulyBusinessModelingVo011();
        vo.setId(modeling.id().value());
        vo.setModelCode(modeling.modelCode());
        vo.setModelName(modeling.modelName());
        vo.setObjectName(modeling.objectName());
        vo.setDataSourceCode(modeling.dataSourceCode());
        vo.setSqlContent(modeling.sqlContent());
        vo.setRemark(modeling.remark());
        vo.setStatus(modeling.status());
        vo.setCreateBy(modeling.audit().createBy());
        vo.setUpdateBy(modeling.audit().updateBy());
        vo.setCreateTime(modeling.audit().createTime());
        vo.setUpdateTime(modeling.audit().updateTime());

        JulyBusinessModelingMetaVo011 meta = new JulyBusinessModelingMetaVo011();
        meta.setObjectName(modeling.content().objectName());
        meta.setDescription(modeling.content().description());
        meta.setObjectType(modeling.content().objectType());
        meta.setPackageName(modeling.content().packageName());
        meta.setImportField(modeling.content().businessField());
        meta.setUrl(modeling.content().routerPath());
        meta.setFieldData(toFieldVoList(modeling.fields()));
        vo.setMetaData(meta);

        return vo;
    }

    /**
     * Map aggregates to response VOs.
     *
     * @param modelings aggregates
     * @return response VO list
     */
    public List<JulyBusinessModelingVo011> toVoList(List<JulyBusinessModeling> modelings) {
        List<JulyBusinessModelingVo011> result = new ArrayList<>();

        for (JulyBusinessModeling modeling : modelings) {
            result.add(toVo(modeling));
        }

        return result;
    }

    /**
     * Map a probe result to its response VO.
     *
     * @param result application probe result
     * @return probe result VO
     */
    public JulyBusinessModelingProbeResultVo011 toProbeResultVo(BusinessModelingProbeResult result) {
        JulyBusinessModelingProbeResultVo011 vo = new JulyBusinessModelingProbeResultVo011();
        vo.setSuccess(result.success());
        vo.setMessage(result.message());
        vo.setFieldData(toFieldVoList(result.fields()));

        return vo;
    }

    /**
     * Map execution rows to the result VO (columns in first-seen order).
     *
     * @param rows result rows
     * @return result VO
     */
    public JulyBusinessModelingResultVo011 toResultVo(List<Map<String, Object>> rows) {
        JulyBusinessModelingResultVo011 vo = new JulyBusinessModelingResultVo011();
        Set<String> columns = new LinkedHashSet<>();

        for (Map<String, Object> row : rows) {
            columns.addAll(row.keySet());
        }

        vo.setColumns(new ArrayList<>(columns));
        vo.setRows(rows);

        return vo;
    }

    /**
     * Map inbound field VOs to domain field definitions, translating validation
     * failures into 400 responses.
     *
     * @param fieldData inbound field list, nullable
     * @return domain field definitions, never null
     */
    public List<JulyMetadataField> toFields(List<JulyBusinessModelingFieldVo011> fieldData) {
        if (fieldData == null || fieldData.isEmpty()) {
            return List.of();
        }

        List<JulyMetadataField> fields = new ArrayList<>();

        for (JulyBusinessModelingFieldVo011 vo : fieldData) {
            try {
                fields.add(new JulyMetadataField(vo.getCode(), vo.getName(), vo.getFieldType(),
                        vo.getLength() == null ? 0 : vo.getLength(),
                        vo.getNotNull() != null && vo.getNotNull(), vo.getDefaultValue(), null));
            } catch (IllegalArgumentException ex) {
                throw BusinessException.badRequest(ex.getMessage());
            }
        }

        return fields;
    }

    /**
     * Map domain fields to response field VOs.
     *
     * @param fields domain fields
     * @return field VO list
     */
    private List<JulyBusinessModelingFieldVo011> toFieldVoList(List<JulyMetadataField> fields) {
        List<JulyBusinessModelingFieldVo011> result = new ArrayList<>();

        for (JulyMetadataField field : fields) {
            JulyBusinessModelingFieldVo011 vo = new JulyBusinessModelingFieldVo011();
            vo.setCode(field.fieldCode());
            vo.setName(field.fieldName());
            vo.setFieldType(field.fieldType());
            vo.setLength(field.fieldLength());
            vo.setNotNull(field.requiredField());
            vo.setDefaultValue(field.defaultValue());
            result.add(vo);
        }

        return result;
    }
}
