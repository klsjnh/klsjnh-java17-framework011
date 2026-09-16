package com.klsjnh.infrastructure.dataservice011.support;

/*                ModelDataCodec011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  model data codec 011 class
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.lowcode011.enums.ObjectType011;
import com.klsjnh.domain.lowcode011.model.FieldInfo011;
import com.klsjnh.domain.lowcode011.model.MetaData011;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Codec between the {@code model_data} JSON column and the low-code description
 * (a {@link MetaData011} owning its {@link FieldInfo011} list).
 * <p>
 * This is the single place that knows the JSON key mapping — notably
 * {@code description} for {@code objectDescription}, {@code importField} for
 * {@code businessField}, {@code url} for {@code routerPath}, and the
 * {@code notNull} ⇄ {@code requiredField} pairing. The domain never sees JSON.
 * </p>
 */

@Component
public class ModelDataCodec011 {

    /**
     * Shared mapper.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Encode the description into the model_data JSON.
     *
     * @param metaData low-code description
     * @return json string
     */
    public String encode(MetaData011 metaData) {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode meta = root.putObject("metaData");
        meta.put("objectName", metaData.objectName());
        meta.put("description", metaData.objectDescription());
        meta.put("objectType", metaData.objectType() == null ? null : metaData.objectType().getCode());
        meta.put("packageName", metaData.packageName());
        meta.put("importField", metaData.businessField());
        meta.put("url", metaData.routerPath());

        ArrayNode fields = root.putArray("fieldData");

        for (FieldInfo011 field : metaData.fields()) {
            ObjectNode node = fields.addObject();
            node.put("code", field.fieldCode());
            node.put("name", field.fieldName());
            node.put("fieldType", field.fieldType());
            node.put("length", field.fieldLength());
            node.put("notNull", field.requiredField());

            if (field.defaultValue() != null) {
                node.put("defaultValue", field.defaultValue());
            }
        }

        try {
            return MAPPER.writeValueAsString(root);
        } catch (Exception ex) {
            throw new IllegalArgumentException("failed to encode model data: " + ex.getMessage(), ex);
        }
    }

    /**
     * Decode the model_data JSON back into the description. A blank column
     * rebuilds an empty description carrying only the object name (the SQL was
     * never probed yet).
     *
     * @param json               model_data json, nullable
     * @param fallbackObjectName object name from the owning column
     * @return low-code description, never null
     */
    public MetaData011 decode(String json, String fallbackObjectName) {
        if (StringUtil011.isBlank(json)) {
            return MetaData011.reconstitute(fallbackObjectName, null, null, null, null, null, List.of(), null, null);
        }

        try {
            JsonNode root = MAPPER.readTree(json);
            JsonNode meta = root.path("metaData");

            ObjectType011 objectType = ObjectType011.fromString(text(meta, "objectType", null));
            List<FieldInfo011> fields = new ArrayList<>();
            JsonNode fieldData = root.path("fieldData");

            if (fieldData.isArray()) {
                for (JsonNode node : fieldData) {
                    fields.add(FieldInfo011.create(text(node, "code", null), text(node, "name", null),
                            text(node, "fieldType", null), node.path("length").asInt(0),
                            node.path("notNull").asBoolean(false), text(node, "defaultValue", null)));
                }
            }

            return MetaData011.reconstitute(text(meta, "objectName", fallbackObjectName), objectType,
                    text(meta, "description", null), text(meta, "importField", null),
                    text(meta, "packageName", null), text(meta, "url", null), fields, null, null);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("failed to decode model data: " + ex.getMessage(), ex);
        }
    }

    /**
     * Read a text field, falling back when absent or null.
     *
     * @param node     json node
     * @param key      field key
     * @param fallback fallback value
     * @return text value or fallback
     */
    private static String text(JsonNode node, String key, String fallback) {
        JsonNode value = node.get(key);

        return value == null || value.isNull() ? fallback : value.asText();
    }
}
