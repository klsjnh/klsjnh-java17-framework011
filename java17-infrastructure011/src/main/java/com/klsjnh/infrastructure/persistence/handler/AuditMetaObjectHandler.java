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

import com.klsjnh.common.constant.AuthAttribute011;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.LocalDateTime;

/**
 * Audit field auto-fill handler for the BasePo columns
 * (create_time / update_time / create_by / update_by). The operator columns are
 * read from the request-scoped attribute set by the auth filter, so no global
 * mutable context is required. Fills only when the field is blank, so pre-set
 * values win.
 */

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    /**
     * Fill create_time / update_time / create_by / update_by on insert.
     *
     * @param metaObject mybatis meta object
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);

        String operatorId = currentOperatorId();

        if (operatorId != null) {
            strictInsertFill(metaObject, "createBy", String.class, operatorId);
            strictInsertFill(metaObject, "updateBy", String.class, operatorId);
        }
    }

    /**
     * Fill update_time / update_by on update.
     *
     * @param metaObject mybatis meta object
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        String operatorId = currentOperatorId();

        if (operatorId != null) {
            strictUpdateFill(metaObject, "updateBy", String.class, operatorId);
        }
    }

    /**
     * Read the authenticated operator id from the current request scope.
     *
     * @return operator user id, or null outside a request thread or when unauthenticated
     */
    private String currentOperatorId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return null;
        }

        Object operatorId = attributes.getAttribute(AuthAttribute011.OPERATOR_ID, RequestAttributes.SCOPE_REQUEST);

        return operatorId == null ? null : operatorId.toString();
    }
}
