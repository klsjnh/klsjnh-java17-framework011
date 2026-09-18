package com.klsjnh.application.lowcode011.runtime;

/*                JulyMetadataRuntimeUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata runtime use case class
 *      2026.09.17  delegate data plane to ObjectTableGateway
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;

import com.klsjnh.domain.iam.UserAuditPort;
import com.klsjnh.domain.lowcode011.CurrentOperatorPort;
import com.klsjnh.domain.lowcode011.records.ResultKey011;
import com.klsjnh.domain.lowcode011.records.MetaDtoKey011;
import com.klsjnh.domain.lowcode011.JulyMetadataVersion;
import com.klsjnh.domain.lowcode011.JulyMetadataVersionRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.menu.JulyMenu;
import com.klsjnh.domain.system011.menu.JulyMenuRepository;
import com.klsjnh.application.lowcode011.ObjectTableGateway;
import com.klsjnh.application.lowcode011.ObjectQueryCommand;
import com.klsjnh.application.lowcode011.designer.JulyMetadataDesignerUseCase;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Runtime use case: runtime menu plus the internal (JWT) dynamic CRUD. The data
 * plane is delegated to {@link ObjectTableGateway}; this class keeps only the
 * menu orchestration, metadata read and the dynamic audit.
 */

@Service
public class JulyMetadataRuntimeUseCase {

    /**
     * Runtime route prefix.
     */
    private static final String ROUTE_PREFIX = "/runtime/";

    /**
     * Designer use case (metadata read).
     */
    private final JulyMetadataDesignerUseCase designerUseCase;

    /**
     * Snapshot repository (menu version).
     */
    private final JulyMetadataVersionRepository versionRepository;

    /**
     * Menu repository (runtime menu entry).
     */
    private final JulyMenuRepository menuRepository;

    /**
     * User audit port (dynamic objectCode for runtime writes).
     */
    private final UserAuditPort userAuditPort;

    /**
     * Object table kernel.
     */
    private final ObjectTableGateway tableGateway;

    /**
     * Current operator port (dynamic audit operator).
     */
    private final CurrentOperatorPort currentOperatorPort;

    /**
     * Create the use case.
     *
     * @param designerUseCase     designer use case
     * @param versionRepository   snapshot repository
     * @param menuRepository      menu repository
     * @param userAuditPort       user audit port
     * @param tableGateway        object table gateway
     * @param currentOperatorPort current operator port
     */
    public JulyMetadataRuntimeUseCase(JulyMetadataDesignerUseCase designerUseCase,
            JulyMetadataVersionRepository versionRepository, JulyMenuRepository menuRepository,
            UserAuditPort userAuditPort, ObjectTableGateway tableGateway, CurrentOperatorPort currentOperatorPort) {
        this.designerUseCase = designerUseCase;
        this.versionRepository = versionRepository;
        this.menuRepository = menuRepository;
        this.userAuditPort = userAuditPort;
        this.tableGateway = tableGateway;
        this.currentOperatorPort = currentOperatorPort;
    }

    /**
     * Publish a published object as a runtime menu (idempotent).
     *
     * @param objectName     object name
     * @param parentMenuCode parent menu code, nullable for root
     * @param projectCode    project code (reserved)
     * @return menu entry
     */
    public Map<String, Object> publishMenu(String objectName, String parentMenuCode, String projectCode) {
        Map<String, Object> metadata = designerUseCase.load(objectName);

        if (versionRepository.findLatest(objectName) == null) {
            throw BusinessException.badRequest("object not published: " + objectName);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> metaData = (Map<String, Object>) metadata.get(MetaDtoKey011.META_DATA);
        String menuName = metaData.get(MetaDtoKey011.DESCRIPTION) == null
                || String.valueOf(metaData.get(MetaDtoKey011.DESCRIPTION)).isBlank()
                ? objectName
                : String.valueOf(metaData.get(MetaDtoKey011.DESCRIPTION));
        String menuCode = "rt_" + objectName;
        String route = ROUTE_PREFIX + objectName;
        String parentId = "";

        if (parentMenuCode != null && !parentMenuCode.isBlank()) {
            JulyMenu parent = menuRepository.findByCode(parentMenuCode);
            if (parent == null) {
                throw BusinessException.badRequest("parent menu not found: " + parentMenuCode);
            }
            parentId = parent.id().value();
        }

        JulyMenu existing = menuRepository.findByCode(menuCode);

        if (existing == null) {
            menuRepository.insert(JulyMenu.create(EntityId.generate(), menuCode, menuName, "2", null, route, null,
                    null, parentId, 9999, AuditInfo.empty()));
        } else {
            existing.updateBasics(menuName, "2", null, route, null, null, parentId, existing.sortOrder());
            menuRepository.update(existing);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put(ResultKey011.OBJECT_NAME, objectName);
        result.put(ResultKey011.MENU_CODE, menuCode);
        result.put(ResultKey011.MENU_NAME, menuName);
        result.put(ResultKey011.ROUTER_PATH, route);
        result.put(ResultKey011.PARENT_MENU_CODE, parentMenuCode);

        return result;
    }

    /**
     * List runtime menus (menus whose route is under {@code /runtime/}).
     *
     * @return runtime menu rows
     */
    public List<Map<String, Object>> listRuntimeMenus() {
        List<Map<String, Object>> rows = new ArrayList<>();

        for (JulyMenu menu : menuRepository.findPage(0, 2000, null)) {
            String route = menu.menuRoute();

            if (route == null || !route.startsWith(ROUTE_PREFIX)) {
                continue;
            }

            String objectName = route.substring(ROUTE_PREFIX.length());
            JulyMetadataVersion latest = versionRepository.findLatest(objectName);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(ResultKey011.OBJECT_NAME, objectName);
            row.put(ResultKey011.MENU_CODE, menu.menuCode());
            row.put(ResultKey011.MENU_NAME, menu.menuName());
            row.put(ResultKey011.ROUTER_PATH, route);
            row.put(ResultKey011.VERSION, latest == null ? null : latest.version());
            row.put(ResultKey011.PUBLISH_STATUS, latest == null ? "draft" : "published");
            rows.add(row);
        }

        return rows;
    }

    /**
     * Object metadata (MetaDTO) for the runtime form.
     *
     * @param objectName object name
     * @return MetaDTO
     */
    public Map<String, Object> getMeta(String objectName) {
        return designerUseCase.load(objectName);
    }

    /**
     * Page rows of a published object.
     *
     * @param objectName object name
     * @param body       query body (filters, pageIndex, pageSize)
     * @return page result
     */
    public Map<String, Object> query(ObjectQueryCommand command) {
        return tableGateway.query(command);
    }

    /**
     * Insert one row.
     *
     * @param objectName object name
     * @param body       row values
     * @param operator   current operator, nullable
     * @return affected rows
     */
    public int create(String objectName, Map<String, Object> body) {
        int rows = tableGateway.insert(objectName, body);
        audit(AuditType011.INSERT, objectName);

        return rows;
    }

    /**
     * Update one row by id / sid.
     *
     * @param objectName object name
     * @param body       key + values
     * @param operator   current operator, nullable
     * @return affected rows
     */
    public int update(String objectName, Map<String, Object> body) {
        int rows = tableGateway.update(objectName, body);
        audit(AuditType011.UPDATE, objectName);

        return rows;
    }

    /**
     * Delete one row by id / sid (logic delete when supported).
     *
     * @param objectName object name
     * @param body       key
     * @param operator   current operator, nullable
     * @return affected rows
     */
    public int delete(String objectName, Map<String, Object> body) {
        int rows = tableGateway.delete(objectName, body);
        audit(AuditType011.DELETE, objectName);

        return rows;
    }

    /**
     * Record an audit row with the dynamic object code; never breaks the write.
     *
     * @param operator   current operator, nullable (debug)
     * @param type       audit type
     * @param objectName object name
     */
    private void audit(AuditType011 type, String objectName) {
        Operator011 operator = currentOperatorPort.current();

        try {
            userAuditPort.record(operator == null ? null : operator.id(),
                    operator == null ? null : operator.userAccount(), type, objectName,
                    "runtime " + type.name().toLowerCase(Locale.ROOT) + " " + objectName,
                    operator == null ? null : operator.ip());
        } catch (Exception ignored) {
            // audit must never break the business write
        }
    }

    /**
     * Read an optional int body value.
     *
     * @param value raw value
     * @return integer or null
     */
    private Integer integer(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }
}
