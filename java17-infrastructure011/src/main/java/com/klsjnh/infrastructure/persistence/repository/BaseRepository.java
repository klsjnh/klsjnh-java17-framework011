package com.klsjnh.infrastructure.persistence.repository;

/*                BaseRepository class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  base repository class
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.vo.BatchDeleteErrorVo011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.infrastructure.persistence.entity.BasePo;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shared implementation base for repository implementations on MyBatis-Plus
 * {@link BaseMapper}: single-table CRUD mechanics, paging, logic delete and
 * batch logic delete.
 * <p>
 * Infrastructure plumbing beneath {@code *RepositoryImpl} — business rules
 * live in domain aggregates, use case orchestration in the application layer,
 * web controllers never touch this class. This is the base tier of the
 * repository base family; the 011 tiers add sort-order capabilities.
 * </p>
 *
 * @param <T> PO type
 * @param <M> mapper type
 */

public abstract class BaseRepository<T extends BasePo, M extends BaseMapper<T>> {

    /**
     * Id charset whitelist for statements that cannot bind parameters.
     */
    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9_-]{1,33}$");

    /**
     * Per-subclass logger, category is the concrete repository class.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * MyBatis-Plus mapper for entity {@code T}.
     */
    protected final M mapper;

    /**
     * Native SQL mapper for maintenance statements.
     */
    protected final CommonMapper commonMapper;

    /**
     * Create the repository base.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    protected BaseRepository(M mapper, CommonMapper commonMapper) {
        this.mapper = mapper;
        this.commonMapper = commonMapper;
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name, a developer constant — never user input
     */
    protected abstract String getTableName();

    /**
     * Insert entity.
     *
     * @param entity entity
     * @return inserted entity
     */
    public T insert(T entity) {
        String funcName = "insert";

        if (entity == null) {
            throw BusinessException.badRequest(funcName + ": entity is required");
        }

        assertBusinessFieldUnique(entity, null);
        mapper.insert(entity);
        logger.info("{} {} {} success ...", funcName, getTableName(), entity.getId());

        return entity;
    }

    /**
     * Update by id.
     *
     * @param entity entity with id
     * @return entity
     */
    public T update(T entity) {
        String funcName = "update";

        String id = requireId(entity, funcName);

        assertBusinessFieldUnique(entity, id);
        int rows = mapper.updateById(entity);

        if (rows == 0) {
            throw BusinessException.recordNotFound(id);
        }

        return entity;
    }

    /**
     * Get by primary key.
     *
     * @param id primary key
     * @return entity or null
     */
    public T getById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        return mapper.selectById(id);
    }

    /**
     * List by wrapper.
     *
     * @param queryWrapper query wrapper
     * @return list
     */
    public List<T> selectList(Wrapper<T> queryWrapper) {
        return mapper.selectList(queryWrapper);
    }

    /**
     * Page query with wrapper.
     *
     * @param pageQuery    page query, null falls back to page 1 / size 10
     * @param queryWrapper query wrapper
     * @return page result
     */
    public PageResult011<T> selectListByPage(PageQuery011 pageQuery, Wrapper<T> queryWrapper) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        Page<T> page = new Page<>(query.pageIndex(), query.pageSize());
        mapper.selectPage(page, queryWrapper);

        return PageResult011.of(query, page.getTotal(), page.getRecords());
    }

    /**
     * Logic delete by id.
     *
     * @param entity entity with id
     * @return id
     */
    public String logicDelete(T entity) {
        String funcName = "logic delete";

        String id = requireId(entity, funcName);
        int rows = mapper.deleteById(id);

        if (rows == 0) {
            throw BusinessException.recordNotFound(id);
        }

        logger.info("{} {} {} success ...", funcName, getTableName(), id);

        return id;
    }

    /**
     * Batch logic delete by primary keys in two statements: one select for
     * existence, one batch delete. Missing ids land in the error list.
     *
     * @param ids primary keys
     * @return per-id success/failure summary
     */
    public BatchDeleteResultVo011 batchLogicDelete(List<String> ids) {
        String funcName = "batch logic delete";

        List<String> normalized = normalizeBatchIds(ids);

        if (normalized.isEmpty()) {
            throw BusinessException.badRequest(funcName + ": ids is required");
        }

        List<T> found = mapper.selectByIds(normalized);
        Set<String> foundIds = new HashSet<>();

        for (T po : found) {
            foundIds.add(po.getId());
        }

        int deleted = 0;
        if (!found.isEmpty()) {
            deleted = mapper.deleteBatchIds(foundIds);
        }

        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        result.setTotal(normalized.size());
        result.setSuccess(deleted);
        result.setFailed(normalized.size() - foundIds.size());

        for (String id : normalized) {
            if (!foundIds.contains(id)) {
                result.getErrors().add(batchError(id, "record not found"));
            }
        }

        logger.info("{} {} total {} success {} failed {} ...",
                funcName, getTableName(), result.getTotal(), result.getSuccess(), result.getFailed());

        return result;
    }

    /**
     * Get by business value.
     *
     * @param businessValue business field value
     * @return entity or null
     */
    public T getByBusinessValue(Object businessValue) {
        if (businessValue == null) {
            return null;
        }

        QueryWrapper<T> w = new QueryWrapper<>();
        w.eq(getBusinessColumn(), businessValue)
                .last("LIMIT 1");

        return mapper.selectOne(w);
    }

    /**
     * Business unique column name.
     *
     * @return column name in snake_case, blank when the table has none
     */
    protected abstract String getBusinessColumn();

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    protected Object getBusinessValue(T entity) {
        return null;
    }

    /**
     * Duplicate message.
     *
     * @return message
     */
    protected String duplicateMessage() {
        return "record already exists";
    }

    /**
     * Assert business field unique.
     *
     * @param entity    entity
     * @param excludeId current id on update; null on insert
     */
    protected void assertBusinessFieldUnique(T entity, String excludeId) {
        String businessField = getBusinessColumn();

        if (businessField == null || businessField.isBlank()) {
            return;
        }

        Object value = getBusinessValue(entity);

        if (value == null || (value instanceof String text && text.isBlank())) {
            return;
        }

        QueryWrapper<T> w = new QueryWrapper<>();
        w.eq(businessField, value);

        if (excludeId != null && !excludeId.isBlank()) {
            w.ne("id", excludeId);
        }

        Long count = mapper.selectCount(w);

        if (count != null && count > 0) {
            throw BusinessException.badRequest(duplicateMessage());
        }
    }

    /**
     * Require id.
     *
     * @param entity   entity
     * @param funcName operation name for the error message
     * @return id
     */
    private String requireId(T entity, String funcName) {
        if (entity == null || entity.getId() == null || entity.getId().isBlank()) {
            throw BusinessException.badRequest(funcName + ": id is required");
        }

        return entity.getId();
    }

    /**
     * Normalize batch ids: trim, drop blanks, deduplicate keeping order.
     *
     * @param ids raw id list
     * @return normalized id list, never null
     */
    protected List<String> normalizeBatchIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> seen = new LinkedHashSet<>();

        for (String id : ids) {
            if (id != null && !id.isBlank()) {
                seen.add(id.trim());
            }
        }

        return new ArrayList<>(seen);
    }

    /**
     * Batch error factory.
     *
     * @param id      primary key
     * @param message safe failure reason
     * @return error detail
     */
    protected BatchDeleteErrorVo011 batchError(String id, String message) {
        BatchDeleteErrorVo011 error = new BatchDeleteErrorVo011();
        error.setId(id);
        error.setMessage(message);

        return error;
    }

    /**
     * Physical delete one row, bypassing the logic-delete interceptor.
     * <p>
     * Maintenance operation only: the id passes the {@link #SAFE_ID} charset
     * whitelist before splicing, because {@link CommonMapper} cannot bind
     * parameters; the table name comes from {@link #getTableName()}, a
     * developer constant.
     * </p>
     *
     * @param id primary key
     */
    public void physicalDelete(String id) {
        String funcName = "physical delete";

        if (id == null || !SAFE_ID.matcher(id).matches()) {
            throw BusinessException.badRequest(funcName + ": invalid id");
        }

        String tableName = getTableName();
        int rows = commonMapper.execute("DELETE FROM " + tableName + " WHERE id = '" + id + "'");

        if (rows == 0) {
            logger.warn("{} {} {} no rows ...", funcName, tableName, id);
        } else {
            logger.info("{} {} {} success ...", funcName, tableName, id);
        }
    }
}
