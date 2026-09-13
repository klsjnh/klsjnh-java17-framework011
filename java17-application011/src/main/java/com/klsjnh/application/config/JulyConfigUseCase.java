package com.klsjnh.application.config;

/*                JulyConfigUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.config.JulyConfig;
import com.klsjnh.domain.system011.config.JulyConfigRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JulyConfig use cases: admin CRUD over runtime key-value parameters, plus
 * getByCode — the program-facing read entry (no HTTP endpoint; readers query
 * the store every time, no cache).
 */

@Service
public class JulyConfigUseCase {

    /**
     * JulyConfig repository.
     */
    private final JulyConfigRepository repository;

    /**
     * Create the use case.
     *
     * @param repository july config repository
     */
    public JulyConfigUseCase(JulyConfigRepository repository) {
        this.repository = repository;
    }

    /**
     * Insert a new config entry.
     *
     * @param code config key, unique
     * @param data config value
     * @return new config id
     */
    @Transactional
    public String insert(String code, String data) {
        if (repository.findEnabledByCode(code) != null) {
            throw BusinessException.badRequest("config code already exists: " + code);
        }

        JulyConfig config = JulyConfig.create(EntityId.generate(), code, data, AuditInfo.empty());
        repository.insert(config);

        return config.id().value();
    }

    /**
     * Update the value of a config entry (code immutable).
     *
     * @param id   config id
     * @param data config value
     * @return config id
     */
    @Transactional
    public String update(String id, String data) {
        JulyConfig config = require(id);
        config.updateData(data);
        repository.update(config);

        return config.id().value();
    }

    /**
     * Logic delete a config entry.
     *
     * @param id config id
     * @return deleted config id
     */
    @Transactional
    public String logicDelete(String id) {
        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Find by primary key.
     *
     * @param id config id
     * @return aggregate
     */
    public JulyConfig getById(String id) {
        return require(id);
    }

    /**
     * Program-facing read entry: the ENABLED config of a key, null when
     * missing or disabled. Readers query the store every time (no cache).
     *
     * @param code config key
     * @return aggregate or null
     */
    public JulyConfig getByCode(String code) {
        return repository.findEnabledByCode(code);
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param keyword   code / data keyword, nullable
     * @return page result
     */
    public PageResult011<JulyConfig> selectListByPage(PageQuery011 pageQuery, String keyword) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        List<JulyConfig> rows = repository.findPage(query.offset(), query.pageSize(), keyword);
        long total = repository.count(keyword);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Require an existing config entry.
     *
     * @param id config id
     * @return aggregate
     */
    private JulyConfig require(String id) {
        JulyConfig config = repository.findById(id);

        if (config == null) {
            throw BusinessException.recordNotFound(id);
        }

        return config;
    }
}
