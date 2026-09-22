package com.klsjnh.infrastructure.iam.user;

/*                UserAuditRecorder class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.22
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  user audit recorder class
 *      2026.09.22  REQUIRES_NEW kept as documented redline exception (017)
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.enums.AuditType011;

import com.klsjnh.domain.iam.user.UserAuditPort;

import com.klsjnh.infrastructure.iam.user.entity.JulyUserAuditPo;
import com.klsjnh.infrastructure.iam.user.mapper.JulyUserAuditMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes user audit rows (append-only). A failed audit write logs a warning
 * and never affects the business transaction.
 * <p>
 * {@code @Transactional(REQUIRES_NEW)} is an explicit tech-debt-redline
 * exception (docs/017 §011.013): the audit row must survive a rollback of the
 * surrounding business transaction. Do not copy this pattern to other
 * infrastructure classes.
 * </p>
 */

@Component
public class UserAuditRecorder implements UserAuditPort {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserAuditRecorder.class);

    /**
     * Audit mapper.
     */
    private final JulyUserAuditMapper mapper;

    /**
     * Create the recorder.
     *
     * @param mapper july user audit mapper
     */
    public UserAuditRecorder(JulyUserAuditMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Insert an audit row; failures are logged and swallowed. Runs in its own
     * transaction (REQUIRES_NEW) so a rollback of the business transaction can
     * never swallow the audit row.
     *
     * @param pkMt         operator user id, nullable (failed logins)
     * @param userAccount  operator account
     * @param auditType    event type
     * @param objectCode   object code, nullable
     * @param content      event description, nullable
     * @param ip           client IP, nullable
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void record(String pkMt, String userAccount, AuditType011 auditType, String objectCode, String content,
            String ip) {
        String funcName = "audit write";

        try {
            JulyUserAuditPo po = new JulyUserAuditPo();
            po.setPkMt(pkMt);
            po.setUserAccount(userAccount);
            po.setAuditType(auditType.getCode());
            po.setObjectCode(objectCode);
            po.setAuditContent(content);
            po.setAuditIp(ip);
            mapper.insert(po);
        } catch (Exception ex) {
            logger.warn("{} type {} account {} failed {} ...", funcName, auditType.getCode(), userAccount,
                    ex.getMessage());
        }
    }
}
