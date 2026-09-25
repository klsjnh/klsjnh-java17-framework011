package com.klsjnh.infrastructure.system011.scheduler.repository;

/*                JulySchedulerAuditRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july scheduler audit repository impl class
 *
 */

import com.klsjnh.domain.system011.scheduler.JulySchedulerAuditRepository;
import com.klsjnh.domain.system011.scheduler.JulySchedulerAuditRow;

import com.klsjnh.infrastructure.system011.scheduler.entity.JulySchedulerAuditPo;
import com.klsjnh.infrastructure.system011.scheduler.mapper.JulySchedulerAuditMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MyBatis-Plus implementation for july_scheduler_audit (append-only write +
 * read by master {@code pk_mt}).
 */

@Repository
public class JulySchedulerAuditRepositoryImpl implements JulySchedulerAuditRepository {

    /**
     * Audit mapper.
     */
    private final JulySchedulerAuditMapper mapper;

    /**
     * Create the repository.
     *
     * @param mapper july scheduler audit mapper
     */
    public JulySchedulerAuditRepositoryImpl(JulySchedulerAuditMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Insert one execution-audit row.
     *
     * @param pkMt          master task id
     * @param schedulerCode task code snapshot, nullable
     * @param startTime     run start
     * @param endTime       run end
     * @param execStatus    SUCCESS / FAIL
     * @param errorMessage  truncated failure hint, nullable
     */
    @Override
    public void insert(String pkMt, String schedulerCode, LocalDateTime startTime, LocalDateTime endTime,
            String execStatus, String errorMessage) {
        JulySchedulerAuditPo po = new JulySchedulerAuditPo();
        po.setPkMt(pkMt);
        po.setSchedulerCode(schedulerCode);
        po.setStartTime(startTime);
        po.setEndTime(endTime);
        po.setExecStatus(execStatus);
        po.setErrorMessage(errorMessage);
        mapper.insert(po);
    }

    /**
     * Offset-based page for one scheduler, newest first.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param pkMt     master task id
     * @return page rows
     */
    @Override
    public List<JulySchedulerAuditRow> findPageByScheduler(int offset, int pageSize, String pkMt) {
        int current = offset / pageSize + 1;
        Page<JulySchedulerAuditPo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, byMasterWrapper(pkMt))
                .getRecords()
                .stream()
                .map(this::toRow)
                .toList();
    }

    /**
     * Count executions for one scheduler.
     *
     * @param pkMt master task id
     * @return total row count
     */
    @Override
    public long countByScheduler(String pkMt) {
        return mapper.selectCount(byMasterWrapper(pkMt));
    }

    /**
     * Filter wrapper: exact master id ({@code pk_mt}), newest start_time first.
     *
     * @param pkMt master task id
     * @return query wrapper
     */
    private QueryWrapper<JulySchedulerAuditPo> byMasterWrapper(String pkMt) {
        QueryWrapper<JulySchedulerAuditPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt);
        wrapper.orderByDesc("start_time").orderByAsc("id");

        return wrapper;
    }

    /**
     * Map a PO to the read model.
     *
     * @param po PO
     * @return read row
     */
    private JulySchedulerAuditRow toRow(JulySchedulerAuditPo po) {
        return new JulySchedulerAuditRow(po.getId(), po.getPkMt(), po.getSchedulerCode(), po.getStartTime(),
                po.getEndTime(), po.getExecStatus(), po.getErrorMessage(), po.getCreateTime());
    }
}
