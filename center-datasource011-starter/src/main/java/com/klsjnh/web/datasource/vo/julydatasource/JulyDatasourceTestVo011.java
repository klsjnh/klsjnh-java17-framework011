package com.klsjnh.web.datasource.vo.julydatasource;

/*                JulyDatasourceTestVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource test vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Connectivity test request VO — covers BOTH probe paths:
 * <ul>
 *   <li>draft: only {@code dbType} + {@code jdbcUrl} are required, the rest is
 *       whatever the form currently holds (nothing is saved);</li>
 *   <li>saved: {@code id} set and everything else blank means "retest the
 *       stored datasource with its stored password".</li>
 * </ul>
 * A non-blank {@code password} always overrides the stored one.
 */

@Data
public class JulyDatasourceTestVo011 {

    /** Datasource id; set means "retest a saved datasource". */
    @Schema(description = "数据源主键（传了表示重测已保存的数据源；不传表示草稿态测试）")
    private String id;

    /** Datasource code, display only in the log. */
    @Schema(description = "数据源编码（仅用于日志展示）")
    private String dsCode;

    /** Database type code, required for the draft path. */
    @Schema(description = "数据库类型（草稿态测试必填）")
    private String dbType;

    /** JDBC url, required for the draft path. */
    @Schema(description = "JDBC URL（草稿态测试必填，须以 jdbc: 开头）")
    private String jdbcUrl;

    /** Login user. */
    @Schema(description = "用户名")
    private String username;

    /** Login password; blank on a saved retest uses the stored one. */
    @Schema(description = "密码（重测已保存数据源时留空则用库里的密码）")
    private String password;

    /** JDBC driver class; blank uses the database type default. */
    @Schema(description = "驱动类名（为空按 dbType 取默认值）")
    private String driverClass;
}
