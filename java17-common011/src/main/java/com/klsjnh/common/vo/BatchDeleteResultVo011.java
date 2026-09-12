package com.klsjnh.common.vo;

/*                BatchDeleteResultVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  batch delete result vo 011 class
 *
 */

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Batch logic delete summary: requested total, success and failed counts, and
 * per-id failure detail.
 */

@Data
public class BatchDeleteResultVo011 {

    /** Requested id count after normalization. */
    private int total;

    /** Successfully logic-deleted count. */
    private int success;

    /** Failed count. */
    private int failed;

    /** Per-id failure detail, never null. */
    private List<BatchDeleteErrorVo011> errors = new ArrayList<>();
}
