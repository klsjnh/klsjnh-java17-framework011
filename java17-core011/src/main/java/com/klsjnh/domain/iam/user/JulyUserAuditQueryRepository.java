package com.klsjnh.domain.iam.user;

/*                JulyUserAuditQueryRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july user audit query repository interface
 *
 */

import com.klsjnh.domain.iam.user.JulyUserAuditRow;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-side port for the append-only user audit trail (CQRS query). Writes go
 * through {@link UserAuditPort}; this port never modifies rows.
 */

public interface JulyUserAuditQueryRepository {

    /**
     * Offset based page query with optional filters.
     *
     * @param offset        zero-based row offset
     * @param pageSize      page size
     * @param accountKeyword operator account keyword, nullable
     * @param auditType     exact event type, nullable
     * @param beginTime     event time lower bound (inclusive), nullable
     * @param endTime       event time upper bound (inclusive), nullable
     * @return page rows
     */
    List<JulyUserAuditRow> findPage(int offset, int pageSize, String accountKeyword, String auditType,
            LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * Count with the same filters as findPage.
     *
     * @param accountKeyword operator account keyword, nullable
     * @param auditType      exact event type, nullable
     * @param beginTime      event time lower bound (inclusive), nullable
     * @param endTime        event time upper bound (inclusive), nullable
     * @return total row count
     */
    long count(String accountKeyword, String auditType, LocalDateTime beginTime, LocalDateTime endTime);
}
