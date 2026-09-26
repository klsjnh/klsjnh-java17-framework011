package com.klsjnh.web.datasource.vo.julydatasource;

/*                JulyDatasourceUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource update vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Update request VO for a datasource entry (dsCode immutable).
 */

@Data
public class JulyDatasourceUpdateVo011 {

    /** Primary key. */
    @NotBlank(message = "id is required")
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Datasource name, max 100. */
    @NotBlank(message = "dsName is required")
    @Schema(description = "数据源名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dsName;

    /** Manual sort order, smaller comes first; blank keeps the stored one. */
    @Schema(description = "排序（越小越靠前，留空保持原值）")
    private Integer sortOrder;

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

    /** Login password, max 300 plaintext; BLANK keeps the stored one (never echoed back; stored as enc:v1:). */
    @Schema(description = "密码（明文最长 300；留空保持原值，落库密文；出参不回显）")
    private String password;

    /** JDBC driver class, max 200; blank uses the database type default. */
    @Schema(description = "驱动类名（最长 200，为空按 dbType 取默认值）")
    private String driverClass;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}
