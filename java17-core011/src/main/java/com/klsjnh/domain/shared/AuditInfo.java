package com.klsjnh.domain.shared;

/*                AuditInfo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  audit info class
 *
 */

import java.time.LocalDateTime;

/**
 * Audit value object mapped to the audit columns
 * (create_by / update_by / create_time / update_time).
 *
 * @param createBy   creator username
 * @param updateBy   last modifier username
 * @param createTime create time
 * @param updateTime last update time
 */

public record AuditInfo(String createBy, String updateBy, LocalDateTime createTime, LocalDateTime updateTime) {

    /**
     * Build an empty audit info.
     *
     * @return empty instance
     */
    public static AuditInfo empty() {
        return new AuditInfo(null, null, null, null);
    }
}
