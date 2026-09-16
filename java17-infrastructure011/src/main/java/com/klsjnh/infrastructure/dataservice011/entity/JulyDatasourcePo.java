package com.klsjnh.infrastructure.dataservice011.entity;

/*                JulyDatasourcePo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Datasource persistence PO mapped to july_datasource (runtime business
 * database connection registry), a sorted table (BasePo011 adds sort_order).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_datasource")
public class JulyDatasourcePo extends BasePo011 {

    /** Datasource code, unique, immutable, doubles as the pool name. */
    private String dsCode;

    /** Datasource name, display only. */
    private String dsName;

    /** Database type code. */
    private String dbType;

    /** JDBC url. */
    private String jdbcUrl;

    /** Schema / database name, optional. */
    private String schemaName;

    /** Login user, optional. */
    private String username;

    /** Login password, optional. */
    private String password;

    /** JDBC driver class, optional. */
    private String driverClass;

    /** Pool config json, optional (reserved). */
    private String poolConfig;

    /** Remark, optional. */
    private String remark;
}
