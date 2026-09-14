package com.klsjnh.application.iam;

/*                JulyUserAuditUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july user audit use case class
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.iam.JulyUserAuditQueryRepository;
import com.klsjnh.domain.iam.JulyUserAuditRow;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JulyUserAudit use cases: read-only page query over the append-only audit
 * trail.
 */

@Service
public class JulyUserAuditUseCase {

    /**
     * Audit query repository.
     */
    private final JulyUserAuditQueryRepository queryRepository;

    /**
     * Create the use case.
     *
     * @param queryRepository audit query repository
     */
    public JulyUserAuditUseCase(JulyUserAuditQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    /**
     * Page query with optional filters.
     *
     * @param pageQuery      page query, null falls back to page 1 / size 10
     * @param accountKeyword operator account keyword, nullable
     * @param auditType      exact event type, nullable
     * @param beginTime      event time lower bound (inclusive), nullable
     * @param endTime        event time upper bound (inclusive), nullable
     * @return page result
     */
    public PageResult011<JulyUserAuditRow> selectListByPage(PageQuery011 pageQuery, String accountKeyword,
            String auditType, LocalDateTime beginTime, LocalDateTime endTime) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        List<JulyUserAuditRow> rows = queryRepository.findPage(query.offset(), query.pageSize(), accountKeyword,
                auditType, beginTime, endTime);
        long total = queryRepository.count(accountKeyword, auditType, beginTime, endTime);

        return PageResult011.of(query, total, rows);
    }
}
