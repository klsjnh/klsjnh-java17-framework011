package com.klsjnh.application.lowcode011.modeling;

/*                BusinessModelingProbeResult class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  business modeling probe result class
 *
 */

import com.klsjnh.domain.lowcode011.JulyMetadataField;

import java.util.List;

/**
 * Result of a probe-and-infer action: the inferred field list when the SQL ran,
 * or a client-safe failure hint. A failure is a normal outcome (the action ran,
 * the target was unusable), so it maps to HTTP 200 with {@code success=false}.
 *
 * @param success whether the SQL could be probed and inferred
 * @param message client-safe hint, never carries credentials
 * @param fields  inferred field definitions, never null
 */

public record BusinessModelingProbeResult(boolean success, String message, List<JulyMetadataField> fields) {

    /**
     * Normalize the field list so a caller never has to null-check it.
     *
     * @param success whether the SQL could be probed and inferred
     * @param message client-safe hint
     * @param fields  inferred field definitions, nullable
     */
    public BusinessModelingProbeResult {
        fields = fields == null ? List.of() : List.copyOf(fields);
    }

    /**
     * Build a success result.
     *
     * @param fields inferred field definitions
     * @return success result
     */
    public static BusinessModelingProbeResult ok(List<JulyMetadataField> fields) {
        return new BusinessModelingProbeResult(true, "probe success", fields);
    }

    /**
     * Build a failure result.
     *
     * @param message client-safe failure hint
     * @return failure result
     */
    public static BusinessModelingProbeResult fail(String message) {
        return new BusinessModelingProbeResult(false, message, null);
    }
}
