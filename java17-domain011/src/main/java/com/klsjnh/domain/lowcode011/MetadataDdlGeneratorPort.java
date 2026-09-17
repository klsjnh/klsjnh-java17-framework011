package com.klsjnh.domain.lowcode011;

/*                MetadataDdlGeneratorPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata ddl generator port
 *
 */

import java.util.List;

/**
 * DDL generation port: turns object metadata into {@code CREATE} statements.
 * Pure and IO-free — the same generator backs {@code previewDdl} and
 * {@code publish}, so a preview is exactly what publishing would execute.
 */

public interface MetadataDdlGeneratorPort {

    /**
     * Generate the CREATE TABLE statement for an object's physical table.
     *
     * @param physicalTable physical table name (already prefixed)
     * @param tableComment  table comment
     * @param fields        object fields
     * @return CREATE TABLE statement (without trailing semicolon)
     */
    String generateCreate(String physicalTable, String tableComment, List<JulyMetadataField> fields);

    /**
     * Generate an {@code ALTER TABLE ... ADD COLUMN} statement for fields that
     * are not present yet. Only {@code ADD COLUMN} is ever produced — no DROP,
     * MODIFY or RENAME.
     *
     * @param physicalTable physical table name (already prefixed)
     * @param fields        fields to add (usually the diff against the live table)
     * @return ALTER TABLE statement (without trailing semicolon)
     */
    String generateAddColumns(String physicalTable, List<JulyMetadataField> fields);
}
