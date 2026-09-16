package com.klsjnh.web.dataservice011.vo.julydatasource;

/*                JulyDatasourceVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Datasource response VO (detail and page rows).
 * <p>
 * There is deliberately NO {@code password} field. The password is write-only:
 * keeping it out of the type is the strongest possible guarantee that no code
 * path can ever echo it back — stronger than nulling a field at the converter,
 * because a typo cannot silently reintroduce it.
 * </p>
 */

@Data
public class JulyDatasourceVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Datasource code, unique, immutable. */
    @Schema(description = "数据源编码（唯一，不可变）")
    private String dsCode;

    /** Manual sort order, smaller comes first. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;

    /** Datasource name. */
    @Schema(description = "数据源名称")
    private String dsName;

    /** Database type code. */
    @Schema(description = "数据库类型（mysql/oracle/sqlserver/postgresql）")
    private String dbType;

    /** JDBC url. */
    @Schema(description = "JDBC URL")
    private String jdbcUrl;

    /** Schema / database name. */
    @Schema(description = "库名或 Schema")
    private String schemaName;

    /** Login user. */
    @Schema(description = "用户名")
    private String username;

    /** JDBC driver class; blank means the database type default applies. */
    @Schema(description = "驱动类名（为空按 dbType 取默认值）")
    private String driverClass;

    /** Pool config json (reserved). */
    @Schema(description = "连接池配置 JSON（本期预留）")
    private String poolConfig;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Row status: 0 disabled / 1 enabled. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;

    /** Creator. */
    @Schema(description = "创建人")
    private String createBy;

    /** Last modifier. */
    @Schema(description = "最后修改人")
    private String updateBy;

    /** Create time. */
    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    /** Update time. */
    @Schema(description = "最后修改日期")
    private LocalDateTime updateTime;
}
