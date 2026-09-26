package com.klsjnh.common.vo;

/*                IdsVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.14
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.14  ids vo 011 class
 *
 */

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Shared batch-id payload VO carried by the unified response envelope.
 * The batch counterpart of {@link IdVo011}: request bodies never carry a bare
 * collection, they carry a named VO with a single "ids" field.
 */

@Data
public class IdsVo011 {

    /** Primary keys of the affected rows. */
    @NotEmpty(message = "ids is required")
    private List<String> ids;
}
