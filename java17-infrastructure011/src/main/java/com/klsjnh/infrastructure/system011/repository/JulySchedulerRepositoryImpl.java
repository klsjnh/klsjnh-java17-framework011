package com.klsjnh.infrastructure.system011.repository;

/*                JulySchedulerRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july scheduler repository impl class
 *
 */

import com.klsjnh.domain.scheduler.JulyScheduler;
import com.klsjnh.domain.scheduler.JulySchedulerRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.infrastructure.system011.entity.JulySchedulerPo;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.system011.mapper.JulySchedulerMapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository implementation for the JulyScheduler aggregate on MyBatis-Plus.
 */

@Repository
public class JulySchedulerRepositoryImpl
        extends BaseRepository<JulySchedulerPo, JulySchedulerMapper>
        implements JulySchedulerRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulySchedulerRepositoryImpl(JulySchedulerMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_scheduler";
    }

    /**
     * Business unique column name.
     *
     * @return column name in snake_case
     */
    @Override
    protected String getBusinessColumn() {
        return "scheduler_code";
    }

    /**
     * Insert a new aggregate.
     *
     * @param scheduler aggregate in stopped state
     */
    @Override
    public void insert(JulyScheduler scheduler) {
        insert(toPo(scheduler));
    }

    /**
     * Update an existing aggregate.
     *
     * @param scheduler aggregate with id
     */
    @Override
    public void update(JulyScheduler scheduler) {
        update(toPo(scheduler));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyScheduler findById(String id) {
        JulySchedulerPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by the unique scheduler code.
     *
     * @param code scheduler code
     * @return aggregate or null
     */
    @Override
    public JulyScheduler findByCode(String code) {
        JulySchedulerPo po = getByBusinessValue(code);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        JulySchedulerPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /**
     * Offset based page query with optional keyword filters.
     *
     * @param offset      zero-based row offset
     * @param pageSize    page size
     * @param codeKeyword scheduler code keyword, nullable
     * @param nameKeyword scheduler name keyword, nullable
     * @return page rows
     */
    @Override
    public List<JulyScheduler> findPage(int offset, int pageSize, String codeKeyword, String nameKeyword) {
        int current = offset / pageSize + 1;
        Page<JulySchedulerPo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, keywordWrapper(codeKeyword, nameKeyword))
                .getRecords()
                .stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Count with the same filters as findPage.
     *
     * @param codeKeyword scheduler code keyword, nullable
     * @param nameKeyword scheduler name keyword, nullable
     * @return total row count
     */
    @Override
    public long count(String codeKeyword, String nameKeyword) {
        return mapper.selectCount(keywordWrapper(codeKeyword, nameKeyword));
    }

    /**
     * Keyword filter wrapper shared by findPage and count.
     *
     * @param codeKeyword scheduler code keyword, nullable
     * @param nameKeyword scheduler name keyword, nullable
     * @return query wrapper
     */
    private QueryWrapper<JulySchedulerPo> keywordWrapper(String codeKeyword, String nameKeyword) {
        QueryWrapper<JulySchedulerPo> wrapper = new QueryWrapper<>();

        if (codeKeyword != null && !codeKeyword.isBlank()) {
            wrapper.like("scheduler_code", codeKeyword);
        }

        if (nameKeyword != null && !nameKeyword.isBlank()) {
            wrapper.like("scheduler_name", nameKeyword);
        }

        wrapper.orderByDesc("create_time").orderByAsc("id");

        return wrapper;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param scheduler aggregate
     * @return PO
     */
    private JulySchedulerPo toPo(JulyScheduler scheduler) {
        JulySchedulerPo po = new JulySchedulerPo();
        po.setId(scheduler.id().value());
        po.setSchedulerCode(scheduler.schedulerCode());
        po.setSchedulerName(scheduler.schedulerName());
        po.setSchedulerHandler(scheduler.schedulerHandler());
        po.setSchedulerCron(scheduler.schedulerCron());
        po.setExecuteTimes(scheduler.executeTimes());
        po.setStatus(scheduler.status());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyScheduler toAggregate(JulySchedulerPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyScheduler(EntityId.of(po.getId()), po.getSchedulerCode(), po.getSchedulerName(),
                po.getSchedulerHandler(), po.getSchedulerCron(), po.getExecuteTimes(), po.getStatus(), audit);
    }
}
