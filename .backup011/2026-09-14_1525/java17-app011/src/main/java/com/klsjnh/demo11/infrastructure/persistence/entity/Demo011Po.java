package com.klsjnh.demo11.infrastructure.persistence.entity;

/*                Demo011Po class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  demo 011 po class
 *
 */

import com.klsjnh.infrastructure.persistence.entity.BasePo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Demo persistence PO mapped to july_demo011 — the reference PO for third
 * parties.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_demo011")
public class Demo011Po extends BasePo {

    /** Demo code, unique. */
    private String code;

    /** Demo name. */
    private String name;
}
