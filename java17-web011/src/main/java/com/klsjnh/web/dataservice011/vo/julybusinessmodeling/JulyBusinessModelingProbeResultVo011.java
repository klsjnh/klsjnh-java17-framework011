package com.klsjnh.web.dataservice011.vo.julybusinessmodeling;

/*                JulyBusinessModelingProbeResultVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling probe result vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Probe result VO: the inferred fields (HTTP always 200, result in payload).
 */

@Data
public class JulyBusinessModelingProbeResultVo011 {

    /** True when the SQL could be probed and inferred. */
    @Schema(description = "是否成功（SQL 可读且推断完成）")
    private boolean success;

    /** Safe human readable detail. */
    @Schema(description = "结果说明")
    private String message;

    /** Inferred field definitions. */
    @Schema(description = "推断出的字段定义列表")
    private List<JulyBusinessModelingFieldVo011> fieldData;
}
