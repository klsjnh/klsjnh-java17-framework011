package com.klsjnh.infrastructure.system011.entity;

import com.klsjnh.infrastructure.persistence.entity.BasePo;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/*                JulyUserAuditPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user audit po class
 *
 */


/**
 * User audit PO mapped to july_user_audit (append-only, pk_mt = operator user
 * id).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_user_audit")
public class JulyUserAuditPo extends BasePo implements MasterLinked {

    /** Master link: operator user id (pk_mt), nullable for failed logins. */
    private String pkMt;

    /** Operator account (redundant for traceability). */
    private String userAccount;

    /** Audit type: LOGIN / LOGIN_FAILED / LOGOUT / INSERT / UPDATE / DELETE / EXPORT. */
    private String auditType;

    /** Object code the operation acted on. */
    private String objectCode;

    /** Event description. */
    private String auditContent;

    /** Client IP. */
    private String auditIp;
}
