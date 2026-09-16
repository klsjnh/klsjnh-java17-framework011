package com.klsjnh.web.dataservice011.vo.julybusinessmodeling;

/*                JulyBusinessModelingResultVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling result vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Execute result VO: the column order plus the rows.
 */

@Data
public class JulyBusinessModelingResultVo011 {

    /** Column labels, in result-set order. */
    @Schema(description = "列名（结果集顺序）")
    private List<String> columns;

    /** Result rows. */
    @Schema(description = "结果行")
    private List<Map<String, Object>> rows;
}
