package com.klsjnh.infrastructure.iam.user.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/*                JulyUserPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user po class
 *
 */


/**
 * User persistence PO mapped to july_user (system management - user table).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_user")
public class JulyUserPo extends BasePo {

    /** Login account, unique. */
    private String userAccount;

    /** User name. */
    private String userName;

    /** Password hash (bcrypt). */
    private String password;

    /** Mobile number. */
    private String mobile;

    /** Email. */
    private String email;

    /** Avatar. */
    private String avatar;

    /** Organization link (pk_org), nullable. */
    private String pkOrg;

    /** Last login time. */
    private LocalDateTime lastLoginTime;
}
