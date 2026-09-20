package com.klsjnh.domain.datasource.kernel;

/*                ColumnMeta record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  column metadata record
 *
 */

/**
 * Column metadata: the neutral description of one column, used to read a source
 * schema, render a target column type and coerce values.
 *
 * @param name      column name
 * @param type      neutral field type code (see {@code FieldTypeCodes011})
 * @param length    character length, nullable when not applicable
 * @param precision numeric precision, nullable when not applicable
 * @param scale     numeric scale, nullable when not applicable
 * @param nullable  whether the column accepts null
 */

public record ColumnMeta(String name, String type, Integer length, Integer precision, Integer scale,
        boolean nullable) {

    /**
     * Whether this column carries a character length.
     *
     * @return true when {@code length} is set and positive
     */
    public boolean hasLength() {
        return length != null && length > 0;
    }
}
