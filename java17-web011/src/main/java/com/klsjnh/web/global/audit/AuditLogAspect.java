package com.klsjnh.web.global.audit;

/*                AuditLogAspect class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.16
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.16  audit log aspect class
 *
 */

import com.klsjnh.common.identity.Operator011;

import com.klsjnh.domain.iam.UserAuditPort;

import com.klsjnh.web.util.Operator011Resolver;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Writes one user-audit row after an annotated controller method returns
 * normally (an exception skips the audit — a failed action is not audited).
 * <p>
 * This is the web-layer IUD audit: the operator (id / account / IP) is only
 * available here, so the application service never carries audit concerns.
 * </p>
 */

@Aspect
@Component
public class AuditLogAspect {

    /**
     * Audit port.
     */
    private final UserAuditPort auditPort;

    /**
     * Create the aspect.
     *
     * @param auditPort audit port
     */
    public AuditLogAspect(UserAuditPort auditPort) {
        this.auditPort = auditPort;
    }

    /**
     * Record the audit row after a successful annotated method.
     *
     * @param joinPoint controller method join point
     * @param auditLog  the method annotation
     */
    @AfterReturning(pointcut = "@annotation(auditLog)")
    public void record(JoinPoint joinPoint, AuditLog auditLog) {
        Operator011 operator = Operator011Resolver.resolve(currentRequest());

        auditPort.record(operator == null ? null : operator.id(), operator == null ? null : operator.userAccount(),
                auditLog.type(), auditLog.objectCode(), auditLog.type().getCode(),
                operator == null ? null : operator.ip());
    }

    /**
     * Current http request, null outside a request thread.
     *
     * @return request or null
     */
    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        return attributes == null ? null : attributes.getRequest();
    }
}
