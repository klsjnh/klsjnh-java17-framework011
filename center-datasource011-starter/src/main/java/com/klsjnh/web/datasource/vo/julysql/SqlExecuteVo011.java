package com.klsjnh.web.datasource.vo.julysql;

/*                SqlExecuteVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  sql execute request (D3 write-access endpoint)
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Authorized single-statement execute request: a dynamic datasource, one
 * statement (no {@code ;}), the bound parameters. Authorization is the
 * {@code datasource:julySql:execute} permission code.
 */

@Data
public class SqlExecuteVo011 {

    /** Dynamic datasource code. */
    @NotBlank(message = "dsCode is required")
    @Schema(description = "数据源编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dsCode;

    /** Single statement with {@code ?} placeholders (no {@code ;}). */
    @NotBlank(message = "sql is required")
    @Schema(description = "单条语句（? 占位，禁分号）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sql;

    /** Bound parameters, nullable for none. */
    @Schema(description = "绑定参数（与 ? 一一对应）")
    private List<Object> params;
}
