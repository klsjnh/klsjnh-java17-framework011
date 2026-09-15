package com.klsjnh.domain.iam;

/*                UserAuditPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  user audit port interface
 *
 */

/**
 * User audit port: the append-only recorder lives in infrastructure. A failed
 * audit write must never affect the business transaction.
 */

public interface UserAuditPort {

    /**
     * Insert an audit row; failures are logged and swallowed by the adapter.
     *
     * @param pkMt        operator user id, nullable
     * @param userAccount operator account, nullable
     * @param auditType   event type (LOGIN / LOGIN_FAILED / LOGOUT / INSERT / UPDATE / DELETE / EXPORT / BACKUP)
     * @param objectCode  object code, nullable
     * @param content     event description, nullable
     * @param ip          client IP, nullable
     */
    void record(String pkMt, String userAccount, String auditType, String objectCode, String content, String ip);
}
