package com.klsjnh.web.system011.vo.julyconfig;

/*                JulyConfigVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Config response VO (detail and page rows).
 */

@Data
public class JulyConfigVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Config key, unique. */
    @Schema(description = "配置项（唯一）")
    private String code;

    /** Config value. */
    @Schema(description = "配置值")
    private String data;

    /** Config status: 0 disabled / 1 enabled. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;

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
