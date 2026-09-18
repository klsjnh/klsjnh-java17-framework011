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
 *      2026.09.17  encode/decode the shared MetadataContent contract
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.records.MetadataContent;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Codec between the {@code model_data} JSON column and the shared low-code
 * contract {@link MetadataContent}. This is the single place that knows the
 * modeling JSON key mapping — notably {@code description} for the object
 * description, {@code importField} for the business field, {@code url} for the
 * router path, and the {@code notNull}/{@code requiredField} pairing. The JSON
 * schema is unchanged (legacy rows decode).
 */

@Component
public class ModelDataCodec011 {

    /**
     * Shared mapper.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Encode the content into the model_data JSON.
     *
     * @param content low-code content contract
     * @return json string
     */
    public String encode(MetadataContent content) {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode meta = root.putObject("metaData");
        meta.put("objectName", content.objectName());
        meta.put("description", content.description());
        meta.put("objectType", content.objectType());
        meta.put("packageName", content.packageName());
        meta.put("importField", content.businessField());
        meta.put("url", content.routerPath());

        ArrayNode fields = root.putArray("fieldData");

        for (JulyMetadataField field : content.fields()) {
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
     * Decode the model_data JSON back into the content contract. A blank column
     * rebuilds an empty description carrying only the object name.
     *
     * @param json               model_data json, nullable
     * @param fallbackObjectName object name from the owning column
     * @return low-code content, never null
     */
    public MetadataContent decode(String json, String fallbackObjectName) {
        if (StringUtil011.isBlank(json)) {
            return MetadataContent.reconstitute(fallbackObjectName, null, null, null, null, null, List.of(), List.of(),
                    List.of());
        }

        try {
            JsonNode root = MAPPER.readTree(json);
            JsonNode meta = root.path("metaData");
            List<JulyMetadataField> fields = new ArrayList<>();
            JsonNode fieldData = root.path("fieldData");

            if (fieldData.isArray()) {
                for (JsonNode node : fieldData) {
                    fields.add(new JulyMetadataField(text(node, "code", null), text(node, "name", null),
                            text(node, "fieldType", null), node.path("length").asInt(0),
                            node.path("notNull").asBoolean(false), text(node, "defaultValue", null), null));
                }
            }

            return MetadataContent.reconstitute(text(meta, "objectName", fallbackObjectName),
                    text(meta, "objectType", null), text(meta, "description", null), text(meta, "importField", null),
                    text(meta, "packageName", null), text(meta, "url", null), fields, List.of(), List.of());
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
