package com.klsjnh.web.lowcode011.converter;

/*                JulyMetadataConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata converter class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataDisplay;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataService;

import com.klsjnh.web.lowcode011.vo.JulyMetadataDisplayVo011;
import com.klsjnh.web.lowcode011.vo.JulyMetadataFieldVo011;
import com.klsjnh.web.lowcode011.vo.JulyMetadataServiceVo011;
import com.klsjnh.web.lowcode011.vo.JulyMetadataVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyMetadata aggregate and the response VO, plus the
 * inbound child VO to domain record mapping.
 */

@Component
public class JulyMetadataConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param metadata aggregate
     * @return response VO
     */
    public JulyMetadataVo011 toVo(JulyMetadata metadata) {
        JulyMetadataVo011 vo = new JulyMetadataVo011();
        vo.setId(metadata.id().value());
        vo.setObjectName(metadata.objectName());
        vo.setSortOrder(metadata.sortOrder());
        vo.setObjectType(metadata.objectType());
        vo.setDescription(metadata.description());
        vo.setBusinessField(metadata.businessField());
        vo.setPackageName(metadata.packageName());
        vo.setRouterPath(metadata.routerPath());
        vo.setRemark(metadata.remark());
        vo.setStatus(metadata.status());
        vo.setCreateBy(metadata.audit().createBy());
        vo.setUpdateBy(metadata.audit().updateBy());
        vo.setCreateTime(metadata.audit().createTime());
        vo.setUpdateTime(metadata.audit().updateTime());

        List<JulyMetadataFieldVo011> fields = new ArrayList<>();

        for (JulyMetadataField field : metadata.fields()) {
            JulyMetadataFieldVo011 item = new JulyMetadataFieldVo011();
            item.setFieldCode(field.fieldCode());
            item.setFieldName(field.fieldName());
            item.setFieldType(field.fieldType());
            item.setFieldLength(field.fieldLength());
            item.setRequiredField(field.requiredField());
            item.setDefaultValue(field.defaultValue());
            item.setSortOrder(field.sortOrder());
            fields.add(item);
        }

        vo.setFields(fields);

        List<JulyMetadataDisplayVo011> displays = new ArrayList<>();

        for (JulyMetadataDisplay display : metadata.displays()) {
            JulyMetadataDisplayVo011 item = new JulyMetadataDisplayVo011();
            item.setDisplayCode(display.displayCode());
            item.setDisplayName(display.displayName());
            item.setAlign(display.align());
            item.setWidth(display.width());
            item.setComponentType(display.componentType());
            item.setDisplayType(display.displayType());
            item.setParam011(display.param011());
            item.setSortOrder(display.sortOrder());
            displays.add(item);
        }

        vo.setDisplays(displays);

        List<JulyMetadataServiceVo011> services = new ArrayList<>();

        for (JulyMetadataService service : metadata.services()) {
            JulyMetadataServiceVo011 item = new JulyMetadataServiceVo011();
            item.setServiceCode(service.serviceCode());
            item.setServiceName(service.serviceName());
            item.setServiceDescription(service.serviceDescription());
            item.setObjectType(service.objectType());
            item.setParamType(service.paramType());
            item.setServiceContent(service.serviceContent());
            item.setEnabled(service.enabled());
            item.setSortOrder(service.sortOrder());
            services.add(item);
        }

        vo.setServices(services);

        return vo;
    }

    /**
     * Map aggregates to response VOs.
     *
     * @param list aggregates
     * @return response VO list
     */
    public List<JulyMetadataVo011> toVoList(List<JulyMetadata> list) {
        List<JulyMetadataVo011> result = new ArrayList<>();

        for (JulyMetadata metadata : list) {
            result.add(toVo(metadata));
        }

        return result;
    }

    /**
     * Map inbound field VOs to domain records.
     *
     * @param fields inbound fields, nullable
     * @return domain fields, never null
     */
    public List<JulyMetadataField> toFields(List<JulyMetadataFieldVo011> fields) {
        if (fields == null) {
            return List.of();
        }

        List<JulyMetadataField> result = new ArrayList<>();

        for (JulyMetadataFieldVo011 vo : fields) {
            try {
                result.add(new JulyMetadataField(vo.getFieldCode(), vo.getFieldName(), vo.getFieldType(),
                        vo.getFieldLength() == null ? 0 : vo.getFieldLength(),
                        vo.getRequiredField() != null && vo.getRequiredField(), vo.getDefaultValue(),
                        vo.getSortOrder()));
            } catch (IllegalArgumentException ex) {
                throw BusinessException.badRequest(ex.getMessage());
            }
        }

        return result;
    }

    /**
     * Map inbound display VOs to domain records.
     *
     * @param displays inbound displays, nullable
     * @return domain displays, never null
     */
    public List<JulyMetadataDisplay> toDisplays(List<JulyMetadataDisplayVo011> displays) {
        if (displays == null) {
            return List.of();
        }

        List<JulyMetadataDisplay> result = new ArrayList<>();

        for (JulyMetadataDisplayVo011 vo : displays) {
            try {
                result.add(new JulyMetadataDisplay(vo.getDisplayCode(), vo.getDisplayName(), vo.getAlign(),
                        vo.getWidth() == null ? 0 : vo.getWidth(), vo.getComponentType(), vo.getDisplayType(),
                        vo.getParam011(), vo.getSortOrder()));
            } catch (IllegalArgumentException ex) {
                throw BusinessException.badRequest(ex.getMessage());
            }
        }

        return result;
    }

    /**
     * Map inbound service VOs to domain records.
     *
     * @param services inbound services, nullable
     * @return domain services, never null
     */
    public List<JulyMetadataService> toServices(List<JulyMetadataServiceVo011> services) {
        if (services == null) {
            return List.of();
        }

        List<JulyMetadataService> result = new ArrayList<>();

        for (JulyMetadataServiceVo011 vo : services) {
            try {
                result.add(new JulyMetadataService(vo.getServiceCode(), vo.getServiceName(),
                        vo.getServiceDescription(), vo.getObjectType(), vo.getParamType(), vo.getServiceContent(),
                        vo.getEnabled() != null && vo.getEnabled(), vo.getSortOrder()));
            } catch (IllegalArgumentException ex) {
                throw BusinessException.badRequest(ex.getMessage());
            }
        }

        return result;
    }
}
