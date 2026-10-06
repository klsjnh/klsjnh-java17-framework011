package com.klsjnh.infrastructure.datasource.sync.writer;

/*                WriterDialect011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  writer dialect unit test (4 shapes + registry fallback)
 *
 */

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

/**
 * Unit tests for the writer dialects: one statement shape per database family
 * and the registry's MySQL fallback.
 */

class WriterDialect011Test {

    /**
     * Sample columns.
     */
    private static final List<String> COLUMNS = List.of("id", "name", "age");

    /**
     * Sample business keys.
     */
    private static final List<String> KEYS = List.of("id");

    /**
     * MySQL keeps the backtick + ON DUPLICATE KEY shape.
     */
    @Test
    void mysqlUpsertShape() {
        MysqlWriterDialect011 dialect = new MysqlWriterDialect011();

        String sql = dialect.buildInsert("t_user", COLUMNS, KEYS, true);

        assertTrue(sql.startsWith("INSERT INTO `t_user` (`id`, `name`, `age`) VALUES (?, ?, ?)"));
        assertTrue(sql.contains("ON DUPLICATE KEY UPDATE `id` = VALUES(`id`)"));
    }

    /**
     * Oracle upsert is a MERGE over dual with dst/src qualifiers.
     */
    @Test
    void oracleMergeShape() {
        OracleWriterDialect011 dialect = new OracleWriterDialect011();

        String sql = dialect.buildInsert("t_user", COLUMNS, KEYS, true);

        assertTrue(sql.startsWith("MERGE INTO t_user dst USING (SELECT ? id, ? name, ? age FROM dual) src"));
        assertTrue(sql.contains("ON (dst.id = src.id)"));
        assertTrue(sql.contains("WHEN MATCHED THEN UPDATE SET dst.name = src.name, dst.age = src.age"));
        assertTrue(sql.contains("WHEN NOT MATCHED THEN INSERT (id, name, age) VALUES (src.id, src.name, src.age)"));
    }

    /**
     * SQL Server MERGE carries HOLDLOCK and the mandatory trailing semicolon.
     */
    @Test
    void sqlserverMergeShape() {
        SqlserverWriterDialect011 dialect = new SqlserverWriterDialect011();

        String sql = dialect.buildInsert("t_user", COLUMNS, KEYS, true);

        assertTrue(sql.startsWith("MERGE INTO t_user WITH (HOLDLOCK) AS dst"));
        assertTrue(sql.endsWith(";"));
        assertTrue(sql.contains("WHEN NOT MATCHED THEN INSERT"));
    }

    /**
     * PostgreSQL upsert is ON CONFLICT with EXCLUDED updates (keys excluded).
     */
    @Test
    void postgresUpsertShape() {
        PostgresqlWriterDialect011 dialect = new PostgresqlWriterDialect011();

        String sql = dialect.buildInsert("t_user", COLUMNS, KEYS, true);

        assertTrue(sql.startsWith("INSERT INTO t_user (id, name, age) VALUES (?, ?, ?)"));
        assertTrue(sql.contains("ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, age = EXCLUDED.age"));
    }

    /**
     * A plain insert (append) carries no upsert clause in every dialect.
     */
    @Test
    void appendInsertHasNoUpsertClause() {
        String mysql = new MysqlWriterDialect011().buildInsert("t", COLUMNS, KEYS, false);
        String oracle = new OracleWriterDialect011().buildInsert("t", COLUMNS, KEYS, false);

        assertTrue(mysql.startsWith("INSERT INTO `t`") && !mysql.contains("ON DUPLICATE"));
        assertTrue(oracle.startsWith("INSERT INTO t") && !oracle.contains("MERGE"));
    }

    /**
     * The registry resolves by normalized type and falls back to MySQL.
     */
    @Test
    void registryFallsBackToMysql() {
        WriterDialectRegistry011 registry = new WriterDialectRegistry011(List.of(new MysqlWriterDialect011(),
                new OracleWriterDialect011(), new SqlserverWriterDialect011(), new PostgresqlWriterDialect011()));

        assertEquals("oracle", registry.resolve("oracle").dbType());
        assertEquals("postgresql", registry.resolve("postgres").dbType());
        assertEquals("mysql", registry.resolve(null).dbType());
        assertEquals("mysql", registry.resolve("clickhouse").dbType());
    }
}
