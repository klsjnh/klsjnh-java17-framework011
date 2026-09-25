package com.klsjnh.application.iam.menu;

/*                JulyMenuUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july menu use case class
 *      2026.09.15  package moved under application/system011
 *      2026.09.15  user menu tree renamed to get
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.domain.iam.role.JulyRole;
import com.klsjnh.domain.iam.role.JulyRoleRepository;
import com.klsjnh.domain.iam.role.JulyUserRoleRepository;
import com.klsjnh.domain.iam.menu.JulyMenu;
import com.klsjnh.domain.iam.menu.JulyMenuRepository;
import com.klsjnh.domain.iam.role.JulyRolePermissionsRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JulyMenu use cases: menu CRUD / tree, plus getUserMenuTree — the
 * login-linked navigation tree (user → roles → granted menus, built-in roles
 * bypass with the full tree).
 */

@Service
public class JulyMenuUseCase {

    /**
     * JulyMenu repository.
     */
    private final JulyMenuRepository repository;

    /**
     * User role junction repository.
     */
    private final JulyUserRoleRepository userRoleRepository;

    /**
     * Role permissions junction repository.
     */
    private final JulyRolePermissionsRepository rolePermissionsRepository;

    /**
     * JulyRole repository.
     */
    private final JulyRoleRepository roleRepository;

    /**
     * Create the use case.
     *
     * @param repository                july menu repository
     * @param userRoleRepository        user role junction repository
     * @param rolePermissionsRepository role permissions junction repository
     * @param roleRepository            july role repository
     */
    public JulyMenuUseCase(JulyMenuRepository repository, JulyUserRoleRepository userRoleRepository,
            JulyRolePermissionsRepository rolePermissionsRepository, JulyRoleRepository roleRepository) {
        this.repository = repository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionsRepository = rolePermissionsRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Insert a new menu node.
     *
     * @param menuCode       menu code, unique
     * @param menuName       menu name
     * @param menuType       menu type ('1' / '2' / '3')
     * @param menuIcon       menu icon, nullable
     * @param menuRoute      menu route, nullable
     * @param permissionCode permission code, nullable
     * @param component      frontend component, nullable
     * @param parentId       parent menu id, blank for root
     * @param sortOrder      sort order
     * @return new menu id
     */
    @Transactional
    public String insert(String menuCode, String menuName, String menuType, String menuIcon, String menuRoute,
            String permissionCode, String component, String parentId, Integer sortOrder) {
        if (repository.findByCode(menuCode) != null) {
            throw BusinessException.badRequest("menu code already exists: " + menuCode);
        }

        JulyMenu menu = JulyMenu.create(EntityId.generate(), menuCode, menuName, menuType, menuIcon, menuRoute,
                permissionCode, component, parentId == null ? "" : parentId, sortOrder, AuditInfo.empty());
        repository.insert(menu);

        return menu.id().value();
    }

    /**
     * Update a menu (code immutable; parent move allowed).
     *
     * @param id             menu id
     * @param menuName       menu name
     * @param menuType       menu type
     * @param menuIcon       menu icon
     * @param menuRoute      menu route
     * @param permissionCode permission code
     * @param component      frontend component
     * @param parentId       parent menu id
     * @param sortOrder      sort order
     * @return menu id
     */
    @Transactional
    public String update(String id, String menuName, String menuType, String menuIcon, String menuRoute,
            String permissionCode, String component, String parentId, Integer sortOrder) {
        JulyMenu menu = require(id);
        menu.updateBasics(menuName, menuType, menuIcon, menuRoute, permissionCode, component,
                parentId == null ? "" : parentId, sortOrder);
        repository.update(menu);

        return menu.id().value();
    }

    /**
     * Logic delete a menu; menus with alive children are rejected.
     *
     * @param id menu id
     * @return deleted menu id
     */
    @Transactional
    public String logicDelete(String id) {
        if (repository.hasChildren(id)) {
            throw BusinessException.badRequest("menu has children, delete children first");
        }

        if (!repository.logicDeleteById(id)) {
            throw BusinessException.notFound("record not found, id=" + id);
        }

        return id;
    }

    /**
     * Logic delete menus, all-or-nothing: a missing id or a menu with children
     * fails the whole batch (404) so the transaction rolls back.
     *
     * @param ids menu ids
     * @return batch delete summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(List<String> ids) {
        List<String> normalized = ids == null ? List.of()
                : ids.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).distinct().toList();

        if (normalized.isEmpty()) {
            throw BusinessException.badRequest("batch logic delete: ids is required");
        }

        for (String id : normalized) {
            if (repository.hasChildren(id)) {
                throw BusinessException.badRequest("menu has children, delete children first");
            }
        }

        repository.logicDeleteByIds(normalized);

        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        result.setTotal(normalized.size());
        result.setSuccess(normalized.size());
        result.setFailed(0);

        return result;
    }

    /**
     * Find by primary key.
     *
     * @param id menu id
     * @return aggregate
     */
    public JulyMenu getById(String id) {
        return require(id);
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param keyword   menu code / name keyword, nullable
     * @return page result
     */
    public PageResult011<JulyMenu> selectListByPage(PageQuery011 pageQuery, String keyword) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        List<JulyMenu> rows = repository.findPage(query.offset(), query.pageSize(), keyword);
        long total = repository.count(keyword);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Load the full alive menu tree.
     *
     * @return root nodes with nested children
     */
    public List<JulyMenu> getTree() {
        return repository.getTree();
    }

    /**
     * Get the navigation menu tree of the current operator: user → roles →
     * granted menus (deduplicated); built-in roles bypass with the full tree.
     * The operator id comes from the auth filter (via the controller).
     *
     * @param operatorId current operator user id
     * @return root nodes with nested children
     */
    public List<JulyMenu> getUserMenuTree(String operatorId) {
        if (operatorId == null || operatorId.isBlank()) {
            throw BusinessException.unauthorized("not authenticated");
        }

        List<String> roleIds = userRoleRepository.findRoleIds(operatorId);

        if (roleIds.isEmpty()) {
            return List.of();
        }

        for (String roleId : roleIds) {
            JulyRole role = roleRepository.findById(roleId);

            if (role != null && "1".equals(role.isBuiltin())) {
                return repository.getTree();
            }
        }

        List<String> menuIds = new ArrayList<>();

        for (String roleId : roleIds) {
            for (String menuId : rolePermissionsRepository.findMenuIds(roleId)) {
                if (!menuIds.contains(menuId)) {
                    menuIds.add(menuId);
                }
            }
        }

        return assembleTree(repository.findByIds(menuIds));
    }

    /**
     * Assemble a flat (sorted) menu list into a tree: nodes whose parent is
     * missing from the set become roots.
     *
     * @param menus flat menu list
     * @return root nodes with nested children
     */
    private List<JulyMenu> assembleTree(List<JulyMenu> menus) {
        List<JulyMenu> sorted = new ArrayList<>(menus);
        sorted.sort(Comparator
                .comparing((JulyMenu m) -> m.sortOrder() == null ? 9999 : m.sortOrder())
                .thenComparing(m -> m.id().value()));

        Map<String, JulyMenu> byId = new LinkedHashMap<>();

        for (JulyMenu menu : sorted) {
            byId.put(menu.id().value(), menu);
        }

        List<JulyMenu> roots = new ArrayList<>();

        for (JulyMenu menu : sorted) {
            String parent = menu.parentId();
            JulyMenu parentNode = parent == null || parent.isBlank() ? null : byId.get(parent);

            if (parentNode == null || parentNode == menu) {
                roots.add(menu);
            } else {
                parentNode.addChild(menu);
            }
        }

        return roots;
    }

    /**
     * Require an existing menu.
     *
     * @param id menu id
     * @return aggregate
     */
    private JulyMenu require(String id) {
        JulyMenu menu = repository.findById(id);

        if (menu == null) {
            throw BusinessException.recordNotFound(id);
        }

        return menu;
    }
}
