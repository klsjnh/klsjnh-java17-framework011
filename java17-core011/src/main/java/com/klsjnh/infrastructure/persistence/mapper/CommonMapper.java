package com.klsjnh.infrastructure.persistence.mapper;

/*                CommonMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  common mapper class
 *
 */

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Native SQL mapper for maintenance statements.
 * <p>
 * <b>Injection guard:</b> the statement is spliced raw ({@code ${sql}}) —
 * never splice user input; identifier values must pass a charset whitelist at
 * the call site (see BaseRepository#physicalDelete).
 * </p>
 */

@Mapper
public interface CommonMapper {

    /**
     * Execute an update/insert/delete statement and return the affected rows.
     *
     * @param sql sql statement
     * @return affected row count
     */
    @Update("${sql}")
    int execute(@Param("sql") String sql);

    /**
     * Run a single-cell count statement and return its value.
     * <p>
     * Used by existence checks that must bypass the logic-delete interceptor
     * (the mapper has no entity, so MyBatis-Plus never appends {@code dr='0'}).
     * Same injection guard as {@link #execute}: never splice user input.
     * </p>
     *
     * @param sql count statement
     * @return the counted value, null when the statement returns no row
     */
    @Select("${sql}")
    Long countBy(@Param("sql") String sql);
}
