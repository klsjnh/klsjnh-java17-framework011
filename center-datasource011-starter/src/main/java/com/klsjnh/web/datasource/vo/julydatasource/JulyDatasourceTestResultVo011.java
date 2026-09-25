package com.klsjnh.web.datasource.vo.julydatasource;

/*                JulyDatasourceTestResultVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource test result vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Connectivity test result VO.
 * <p>
 * The envelope's {@code statusCode} stays 200 even when {@code success} is
 * false: the probe ACTION succeeded (the server reached a verdict), the target
 * was simply unreachable. Callers branch on {@code success}, not on the status
 * code.
 * </p>
 */

@Data
public class JulyDatasourceTestResultVo011 {

    /** Whether a connection could be opened. */
    @Schema(description = "是否连通")
    private boolean success;

    /** Human readable hint (never carries the password). */
    @Schema(description = "结果说明")
    private String message;

    /** Database product name, null on failure. */
    @Schema(description = "数据库产品名（失败为空）")
    private String databaseProduct;

    /** Database product version, null on failure. */
    @Schema(description = "数据库版本（失败为空）")
    private String databaseVersion;
}
