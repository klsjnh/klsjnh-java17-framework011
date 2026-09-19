package com.klsjnh.infrastructure.system011.organization.entity;

/*                JulyOrganizationPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.TreePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Organization persistence PO mapped to july_organization (tree + sibling
 * order + leader link).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_organization")
public class JulyOrganizationPo extends TreePo011<JulyOrganizationPo> {

    /** Organization code, unique. */
    private String orgCode;

    /** Organization name. */
    private String orgName;

    /** Leader user id (pk_user → july_user.id), nullable. */
    private String pkUser;

    /** Redundant tree level (root = 1). */
    private Integer orgLevel;
}
