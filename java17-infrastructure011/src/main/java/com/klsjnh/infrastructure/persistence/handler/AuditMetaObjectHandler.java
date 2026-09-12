package com.klsjnh.infrastructure.persistence.handler;

/*                AuditMetaObjectHandler class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  audit meta object handler class
 *
 */

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Audit field auto-fill handler for the BasePo columns
 * (create_time / update_time; create_by / update_by join in with the login
 * context). Fills only when the field is blank, so pre-set values win.
 */

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    /**
     * Fill create_time / update_time on insert.
     *
     * @param metaObject mybatis meta object
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * Fill update_time on update.
     *
     * @param metaObject mybatis meta object
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
