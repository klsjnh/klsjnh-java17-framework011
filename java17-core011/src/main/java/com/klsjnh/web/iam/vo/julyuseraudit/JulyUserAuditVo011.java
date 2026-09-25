package com.klsjnh.web.iam.vo.julyuseraudit;

/*                JulyUserAuditVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july user audit vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * User audit response VO (page rows of the append-only trail).
 */

@Data
public class JulyUserAuditVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Operator user id (pk_mt, nullable for failed logins). */
    @Schema(description = "操作者 id（pk_mt，登录失败可空）")
    private String pkMt;

    /** Operator account (redundant). */
    @Schema(description = "操作者账号（冗余）")
    private String userAccount;

    /** Event type. */
    @Schema(description = "事件类型")
    private String auditType;

    /** Object code. */
    @Schema(description = "对象编码")
    private String objectCode;

    /** Event description. */
    @Schema(description = "事件描述")
    private String auditContent;

    /** Client IP. */
    @Schema(description = "客户端 IP")
    private String auditIp;

    //** Event time (the create time). */
    @Schema(description = "事件时间（即创建时间）")
    private LocalDateTime createTime;
}
