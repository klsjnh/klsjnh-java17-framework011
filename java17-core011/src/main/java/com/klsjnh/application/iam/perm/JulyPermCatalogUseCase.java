package com.klsjnh.application.iam.perm;

/*                JulyPermCatalogUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  permission catalog query / ensure
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.iam.perm.JulyPermAction;
import com.klsjnh.domain.iam.perm.JulyPermActionRepository;
import com.klsjnh.domain.iam.perm.JulyPermObject;
import com.klsjnh.domain.iam.perm.JulyPermObjectRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Permission catalog use cases: list objects / actions; ensure seed rows.
 */

@Service
public class JulyPermCatalogUseCase {

    /**
     * Object catalog.
     */
    private final JulyPermObjectRepository objectRepository;

    /**
     * Action catalog.
     */
    private final JulyPermActionRepository actionRepository;

    /**
     * Create the use case.
     *
     * @param objectRepository object catalog
     * @param actionRepository action catalog
     */
    public JulyPermCatalogUseCase(JulyPermObjectRepository objectRepository,
            JulyPermActionRepository actionRepository) {
        this.objectRepository = objectRepository;
        this.actionRepository = actionRepository;
    }

    /**
     * Every enabled permission object.
     *
     * @return objects
     */
    public List<JulyPermObject> listObjects() {
        return objectRepository.findAllEnabled();
    }

    /**
     * Enabled actions under one object.
     *
     * @param objectCode object code
     * @return actions
     */
    public List<JulyPermAction> listActions(String objectCode) {
        if (StringUtil011.isBlank(objectCode)) {
            throw BusinessException.badRequest("objectCode is required");
        }

        if (objectRepository.findByCode(objectCode.trim()) == null) {
            throw BusinessException.badRequest("permission object not found: " + objectCode);
        }

        return actionRepository.findEnabledByObjectCode(objectCode.trim());
    }

    /**
     * Ensure an object row exists (idempotent seed helper).
     *
     * @param objectCode object code
     * @param objectName display name
     * @param moduleCode module
     * @param sortOrder  sort
     * @return object id
     */
    @Transactional
    public String ensureObject(String objectCode, String objectName, String moduleCode, int sortOrder) {
        JulyPermObject existing = objectRepository.findByCode(objectCode);

        if (existing != null) {
            return existing.id().value();
        }

        JulyPermObject object = JulyPermObject.create(EntityId.generate(), objectCode, objectName, moduleCode,
                sortOrder, null, AuditInfo.empty());
        objectRepository.insert(object);

        return object.id().value();
    }

    /**
     * Ensure an action row exists (idempotent seed helper).
     *
     * @param objectCode     object code
     * @param actionCode     action code
     * @param actionName     display name
     * @param permissionCode full permission code
     * @param sortOrder      sort
     * @return action id
     */
    @Transactional
    public String ensureAction(String objectCode, String actionCode, String actionName, String permissionCode,
            int sortOrder) {
        JulyPermAction existing = actionRepository.findByPermissionCode(permissionCode);

        if (existing != null) {
            return existing.id().value();
        }

        JulyPermAction byPair = actionRepository.findByObjectAndAction(objectCode, actionCode);

        if (byPair != null) {
            return byPair.id().value();
        }

        JulyPermAction action = JulyPermAction.create(EntityId.generate(), objectCode, actionCode, actionName,
                permissionCode, sortOrder, null, AuditInfo.empty());
        actionRepository.insert(action);

        return action.id().value();
    }
}
