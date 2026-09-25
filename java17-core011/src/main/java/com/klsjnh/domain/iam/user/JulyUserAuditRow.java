package com.klsjnh.domain.iam.user;

/*                JulyUserAuditRow record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july user audit row record
 *
 */

import java.time.LocalDateTime;

/**
 * Audit read model (CQRS query row): one event of the append-only trail.
 *
 * @param id           primary key
 * @param pkMt         operator user id, nullable
 * @param userAccount  operator account, nullable
 * @param auditType    event type
 * @param objectCode   object code, nullable
 * @param auditContent event description, nullable
 * @param auditIp      client IP, nullable
 * @param createTime   event time
 */

public record JulyUserAuditRow(String id, String pkMt, String userAccount, String auditType, String objectCode,
        String auditContent, String auditIp, LocalDateTime createTime) {
}
