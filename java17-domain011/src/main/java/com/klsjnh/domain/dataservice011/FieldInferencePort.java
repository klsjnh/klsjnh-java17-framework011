package com.klsjnh.domain.dataservice011;

/*                FieldInferencePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  field inference port interface
 *
 */

import com.klsjnh.domain.lowcode011.model.FieldInfo011;

import java.util.List;

/**
 * Field inference port: turns a raw probe outcome (JDBC column metadata) into
 * low-code field definitions, completing the common column set. Declared in the
 * domain so the application layer can infer without depending on
 * infrastructure; the implementation lives in infrastructure.
 */

public interface FieldInferencePort {

    /**
     * Infer the field list from a probe outcome.
     *
     * @param outcome probe outcome
     * @return inferred field definitions with common columns completed
     */
    List<FieldInfo011> infer(ProbeOutcome outcome);
}
