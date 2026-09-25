package com.klsjnh.web.global.audit;

/*                AuditLog annotation
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.16
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.16  audit log annotation
 *
 */

import com.klsjnh.common.enums.AuditType011;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method whose successful return writes one user-audit row.
 * <p>
 * Placed on controller IUD actions only (insert / update / logicDelete); the
 * application service stays free of audit concerns. Queries, export, backup and
 * auth events are not annotated — their audit (if any) lives with the action.
 * </p>
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * Audit event type.
     *
     * @return audit type
     */
    AuditType011 type();

    /**
     * Object code the action targets (e.g. julyConfig).
     *
     * @return object code
     */
    String objectCode();
}
