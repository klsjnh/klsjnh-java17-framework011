package com.klsjnh.demo11.application;

/*                Demo011UseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  demo 011 use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.demo11.domain.Demo011;
import com.klsjnh.demo11.domain.Demo011Repository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Demo011 use cases: reference CRUD orchestration for third parties.
 */

@Service
public class Demo011UseCase {

    /**
     * Demo011 repository.
     */
    private final Demo011Repository repository;

    /**
     * Create the use case.
     *
     * @param repository demo repository
     */
    public Demo011UseCase(Demo011Repository repository) {
        this.repository = repository;
    }

    /**
     * Insert a new demo row.
     *
     * @param code demo code, unique
     * @param name demo name
     * @return new id
     */
    @Transactional
    public String insert(String code, String name) {
        if (repository.findByCode(code) != null) {
            throw BusinessException.badRequest("demo code already exists: " + code);
        }

        Demo011 demo = Demo011.create(EntityId.generate(), code, name, AuditInfo.empty());
        repository.insert(demo);

        return demo.id().value();
    }

    /**
     * Update the name of a demo row.
     *
     * @param id   primary key
     * @param name demo name
     * @return id
     */
    @Transactional
    public String update(String id, String name) {
        Demo011 demo = require(id);
        demo.updateName(name);
        repository.update(demo);

        return demo.id().value();
    }

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return deleted id
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
     * @param id primary key
     * @return aggregate
     */
    public Demo011 getById(String id) {
        return require(id);
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param keyword   code / name keyword, nullable
     * @return page result
     */
    public PageResult011<Demo011> selectListByPage(PageQuery011 pageQuery, String keyword) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        List<Demo011> rows = repository.findPage(query.offset(), query.pageSize(), keyword);
        long total = repository.count(keyword);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Require an existing demo row.
     *
     * @param id primary key
     * @return aggregate
     */
    private Demo011 require(String id) {
        Demo011 demo = repository.findById(id);

        if (demo == null) {
            throw BusinessException.recordNotFound(id);
        }

        return demo;
    }
}
