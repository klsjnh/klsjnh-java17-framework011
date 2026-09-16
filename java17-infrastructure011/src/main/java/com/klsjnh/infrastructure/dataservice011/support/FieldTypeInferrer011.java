package com.klsjnh.infrastructure.dataservice011.support;

/*                FieldTypeInferrer011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  field type inferrer 011 class
 *
 */

import com.klsjnh.domain.dataservice011.FieldInferencePort;
import com.klsjnh.domain.dataservice011.ProbeOutcome;
import com.klsjnh.domain.lowcode011.model.FieldInfo011;

import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Turns probed column metadata into low-code field definitions: maps the JDBC
 * type to a {@code FieldType011} code, resolves the common columns to their
 * dedicated types, and completes the common column set so the generator always
 * receives a full table shape.
 */

@Component
public class FieldTypeInferrer011 implements FieldInferencePort {

    /**
     * Structural columns placed in the fixed order
     * {@code id → business → status → audit four → dr}.
     */
    private static final Set<String> STRUCTURAL = Set.of(
            "id", "status", "create_by", "update_by", "create_time", "update_time", "dr");

    /**
     * Common column to dedicated field type mapping (single validation source).
     */
    private static final Map<String, String> COMMON_TYPE = Map.ofEntries(
            Map.entry("id", "id"),
            Map.entry("status", "status"),
            Map.entry("create_by", "create_by"),
            Map.entry("update_by", "update_by"),
            Map.entry("create_time", "create_time"),
            Map.entry("update_time", "update_time"),
            Map.entry("dr", "status"),
            Map.entry("sort_order", "int"),
            Map.entry("parent_id", "string"),
            Map.entry("pk_mt", "string"));

    /**
     * Common column display names (Chinese, per project convention).
     */
    private static final Map<String, String> COMMON_NAME = Map.ofEntries(
            Map.entry("id", "主键"),
            Map.entry("status", "状态"),
            Map.entry("create_by", "创建人"),
            Map.entry("update_by", "最后修改人"),
            Map.entry("create_time", "创建日期"),
            Map.entry("update_time", "最后修改日期"),
            Map.entry("dr", "删除标记"),
            Map.entry("sort_order", "排序"),
            Map.entry("parent_id", "上级"),
            Map.entry("pk_mt", "主表链接"));

    /**
     * Length rounding steps for string columns.
     */
    private static final int[] LENGTH_STEPS = {50, 100, 150, 255, 300, 500, 1000, 2000, 4000, 8000};

    /**
     * Infer the final field list from a probe outcome: probed business fields
     * in select order, with the common columns completed in the base-entity
     * order (missing ones synthesized).
     *
     * @param outcome probe outcome
     * @return final field list with common columns completed
     */
    @Override
    public List<FieldInfo011> infer(ProbeOutcome outcome) {
        Map<String, FieldInfo011> probed = new LinkedHashMap<>();

        for (ProbeOutcome.ProbeColumn column : outcome.columns()) {
            if (column.code() != null && !column.code().isBlank() && !probed.containsKey(column.code())) {
                probed.put(column.code(), toField(column));
            }
        }

        List<FieldInfo011> result = new ArrayList<>();
        result.add(orSynth(probed, "id"));

        for (Map.Entry<String, FieldInfo011> entry : probed.entrySet()) {
            if (!STRUCTURAL.contains(entry.getKey())) {
                result.add(entry.getValue());
            }
        }

        result.add(orSynth(probed, "status"));
        result.add(orSynth(probed, "create_by"));
        result.add(orSynth(probed, "update_by"));
        result.add(orSynth(probed, "create_time"));
        result.add(orSynth(probed, "update_time"));
        result.add(orSynth(probed, "dr"));

        return result;
    }

    /**
     * Map one probed column to a field definition.
     *
     * @param column probed column
     * @return field definition
     */
    private FieldInfo011 toField(ProbeOutcome.ProbeColumn column) {
        String code = column.code();
        String special = COMMON_TYPE.get(code);
        String type = special != null ? special : jdbcTypeToFieldType(column.jdbcType(), column.length());
        String name = COMMON_NAME.getOrDefault(code, code);

        return FieldInfo011.create(code, name, type, lengthFor(type, column.length()), !column.nullable(), null);
    }

    /**
     * Take the probed common column or synthesize the missing one.
     *
     * @param probed probed fields by code
     * @param code   common column code
     * @return field definition
     */
    private FieldInfo011 orSynth(Map<String, FieldInfo011> probed, String code) {
        FieldInfo011 existing = probed.get(code);

        return existing != null ? existing : synth(code);
    }

    /**
     * Synthesize a missing common column.
     *
     * @param code common column code
     * @return field definition
     */
    private FieldInfo011 synth(String code) {
        String type = COMMON_TYPE.get(code);
        boolean required = !"create_by".equals(code) && !"update_by".equals(code);

        return FieldInfo011.create(code, COMMON_NAME.get(code), type, nominalLength(type), required, null);
    }

    /**
     * Map a JDBC type to a field type code, always inside {@code FieldType011}.
     *
     * @param jdbcType raw {@code java.sql.Types} constant
     * @param length   reported display size
     * @return field type code
     */
    private String jdbcTypeToFieldType(int jdbcType, int length) {
        if ((jdbcType == Types.TINYINT || jdbcType == Types.BIT) && length == 1) {
            return "boolean";
        }

        switch (jdbcType) {
            case Types.LONGVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "text";
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return "int";
            case Types.DECIMAL:
            case Types.NUMERIC:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.REAL:
                return "float";
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return "date";
            case Types.BOOLEAN:
            case Types.BIT:
                return "boolean";
            default:
                return "string";
        }
    }

    /**
     * Resolve the field length for a type.
     *
     * @param type     field type code
     * @param reported reported display size
     * @return field length
     */
    private int lengthFor(String type, int reported) {
        if ("string".equals(type)) {
            return roundLength(reported);
        }

        return nominalLength(type);
    }

    /**
     * Nominal length of a structural type.
     *
     * @param type field type code
     * @return nominal length
     */
    private int nominalLength(String type) {
        switch (type) {
            case "id":
            case "create_by":
            case "update_by":
                return 33;
            case "status":
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Round a string length up to the nearest common bucket.
     *
     * @param length reported length
     * @return rounded length
     */
    private int roundLength(int length) {
        if (length <= 0) {
            return 255;
        }

        for (int step : LENGTH_STEPS) {
            if (length <= step) {
                return step;
            }
        }

        return length;
    }
}
