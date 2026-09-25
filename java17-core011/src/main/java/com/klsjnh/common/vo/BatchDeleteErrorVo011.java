package com.klsjnh.common.vo;

/*                BatchDeleteErrorVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  batch delete error vo 011 class
 *
 */

import lombok.Data;

/**
 * Per-id failure detail of a batch logic delete.
 */

@Data
public class BatchDeleteErrorVo011 {

    /** Primary key of the failed row. */
    private String id;

    /** Safe failure reason. */
    private String message;
}
