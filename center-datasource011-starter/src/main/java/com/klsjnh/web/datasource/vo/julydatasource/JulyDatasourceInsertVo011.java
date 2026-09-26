package com.klsjnh.web.datasource.vo.julydatasource;

/*                JulyDatasourceInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Insert request VO for a datasource entry (dsCode required and immutable).
 */

@Data
public class JulyDatasourceInsertVo011 {

    /** Datasource code, unique, immutable, max 60. */
    @NotBlank(message = "dsCode is required")
    @Schema(description = "数据源编码（唯一，最长 60，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dsCode;

    /** Manual sort order, smaller comes first; blank falls back to the default. */
    @Schema(description = "排序（越小越靠前，留空取默认 9999）")
    private Integer sortOrder;

    /** Datasource name, max 100. */
    @NotBlank(message = "dsName is required")
    @Schema(description = "数据源名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dsName;

    /** Database type code. */
    @NotBlank(message = "dbType is required")
    @Schema(description = "数据库类型（mysql/oracle/sqlserver/postgresql）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String dbType;

    /** JDBC url, max 500, must start with jdbc:. */
    @NotBlank(message = "jdbcUrl is required")
    @Schema(description = "JDBC URL（须以 jdbc: 开头，最长 500）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jdbcUrl;

    /** Schema / database name, max 60. */
    @Schema(description = "库名或 Schema（最长 60）")
    private String schemaName;

    /** Login user, max 100. */
    @Schema(description = "用户名（最长 100）")
    private String username;

    /** Login password, max 300 plaintext; write-only, never echoed back (stored as enc:v1:). */
    @Schema(description = "密码（明文最长 300，落库密文；出参不回显）")
    private String password;

    /** JDBC driver class, max 200; blank uses the database type default. */
    @Schema(description = "驱动类名（最长 200，为空按 dbType 取默认值）")
    private String driverClass;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}
