package com.klsjnh.infrastructure.iam.user.repository;

/*                JulyUserAuditQueryRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july user audit query repository impl class
 *
 */

import com.klsjnh.domain.iam.user.JulyUserAuditQueryRepository;
import com.klsjnh.domain.iam.user.JulyUserAuditRow;

import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.iam.user.entity.JulyUserAuditPo;
import com.klsjnh.infrastructure.iam.user.mapper.JulyUserAuditMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-side implementation for the user audit trail on MyBatis-Plus (query
 * only — the append-only write path is {@code UserAuditRecorder}).
 */

@Repository
public class JulyUserAuditQueryRepositoryImpl implements JulyUserAuditQueryRepository {

    /**
     * Audit mapper.
     */
    private final JulyUserAuditMapper mapper;

    /**
     * Create the query repository.
     *
     * @param mapper july user audit mapper
     */
    public JulyUserAuditQueryRepositoryImpl(JulyUserAuditMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Offset based page query with optional filters.
     *
     * @param offset         zero-based row offset
     * @param pageSize       page size
     * @param accountKeyword operator account keyword, nullable
     * @param auditType      exact event type, nullable
     * @param beginTime      event time lower bound (inclusive), nullable
     * @param endTime        event time upper bound (inclusive), nullable
     * @return page rows
     */
    @Override
    public List<JulyUserAuditRow> findPage(int offset, int pageSize, String accountKeyword, String auditType,
            LocalDateTime beginTime, LocalDateTime endTime) {
        int current = offset / pageSize + 1;
        Page<JulyUserAuditPo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, filterWrapper(accountKeyword, auditType, beginTime, endTime))
                .getRecords()
                .stream()
                .map(this::toRow)
                .toList();
    }

    /**
     * Count with the same filters as findPage.
     *
     * @param accountKeyword operator account keyword, nullable
     * @param auditType      exact event type, nullable
     * @param beginTime      event time lower bound (inclusive), nullable
     * @param endTime        event time upper bound (inclusive), nullable
     * @return total row count
     */
    @Override
    public long count(String accountKeyword, String auditType, LocalDateTime beginTime, LocalDateTime endTime) {
        return mapper.selectCount(filterWrapper(accountKeyword, auditType, beginTime, endTime));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param accountKeyword operator account keyword, nullable
     * @param auditType      exact event type, nullable
     * @param beginTime      event time lower bound, nullable
     * @param endTime        event time upper bound, nullable
     * @return query wrapper
     */
    private QueryWrapper<JulyUserAuditPo> filterWrapper(String accountKeyword, String auditType,
            LocalDateTime beginTime, LocalDateTime endTime) {
        QueryWrapper<JulyUserAuditPo> wrapper = new QueryWrapper<>();

        if (accountKeyword != null && !accountKeyword.isBlank()) {
            wrapper.like("user_account", accountKeyword);
        }

        if (auditType != null && !auditType.isBlank()) {
            wrapper.eq("audit_type", auditType);
        }

        if (beginTime != null) {
            wrapper.ge("create_time", beginTime);
        }

        if (endTime != null) {
            wrapper.le("create_time", endTime);
        }

        wrapper.orderByDesc("create_time").orderByAsc("id");

        return wrapper;
    }

    /**
     * Map a PO to the read model.
     *
     * @param po PO
     * @return read row
     */
    private JulyUserAuditRow toRow(JulyUserAuditPo po) {
        return new JulyUserAuditRow(po.getId(), po.getPkMt(), po.getUserAccount(), po.getAuditType(),
                po.getObjectCode(), po.getAuditContent(), po.getAuditIp(), po.getCreateTime());
    }
}
