package com.klsjnh.application.system011.scheduler;

/*                JulySchedulerAuditUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july scheduler audit use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.system011.scheduler.JulySchedulerAuditRepository;
import com.klsjnh.domain.system011.scheduler.JulySchedulerAuditRow;
import com.klsjnh.domain.system011.scheduler.JulySchedulerPermissionCodes011;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JulyScheduler execution-audit use cases: read-only page query of run
 * outcomes by master scheduler id. Permission reuses
 * {@link JulySchedulerPermissionCodes011#SELECT}.
 */

@Service
public class JulySchedulerAuditUseCase {

    /**
     * Audit repository.
     */
    private final JulySchedulerAuditRepository auditRepository;

    /**
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * Create the use case.
     *
     * @param auditRepository   audit repository
     * @param authorizationPort authorization port
     */
    public JulySchedulerAuditUseCase(JulySchedulerAuditRepository auditRepository,
            AuthorizationPort authorizationPort) {
        this.auditRepository = auditRepository;
        this.authorizationPort = authorizationPort;
    }

    /**
     * Page query of executions for one scheduler definition.
     *
     * @param operatorId operator user id
     * @param pageQuery  page query, null falls back to page 1 / size 10
     * @param pkMt       master task id ({@code pk_mt}, required)
     * @return page result
     */
    public PageResult011<JulySchedulerAuditRow> selectListByPage(String operatorId, PageQuery011 pageQuery,
            String pkMt) {
        authorizationPort.assertHas(operatorId, JulySchedulerPermissionCodes011.SELECT);

        if (pkMt == null || pkMt.isBlank()) {
            throw BusinessException.badRequest("pkMt is required");
        }

        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        String masterId = pkMt.trim();
        List<JulySchedulerAuditRow> rows = auditRepository.findPageByScheduler(query.offset(), query.pageSize(),
                masterId);
        long total = auditRepository.countByScheduler(masterId);

        return PageResult011.of(query, total, rows);
    }
}
