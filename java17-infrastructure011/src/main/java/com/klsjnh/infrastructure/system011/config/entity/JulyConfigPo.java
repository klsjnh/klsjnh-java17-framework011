package com.klsjnh.infrastructure.system011.config.entity;

/*                JulyConfigPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Config persistence PO mapped to july_config (runtime key-value parameter).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_config")
public class JulyConfigPo extends BasePo {

    /** Config key, unique. */
    private String code;

    /** Config value (String; consumer parses). */
    private String data;
}
