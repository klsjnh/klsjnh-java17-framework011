package com.klsjnh.infrastructure.system011.repository;

/*                JulyMenuRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july menu repository impl class
 *
 */

import com.klsjnh.domain.system011.menu.JulyMenu;
import com.klsjnh.domain.system011.menu.JulyMenuRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseTreeRepository011;
import com.klsjnh.infrastructure.system011.entity.JulyMenuPo;
import com.klsjnh.infrastructure.system011.mapper.JulyMenuMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for the JulyMenu aggregate on the tree + sort
 * base (BaseTreeRepository011).
 */

@Repository
public class JulyMenuRepositoryImpl
        extends BaseTreeRepository011<JulyMenuPo, JulyMenuMapper>
        implements JulyMenuRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyMenuRepositoryImpl(JulyMenuMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_menu";
    }

    /**
     * Business unique column name.
     *
     * @return column name in snake_case
     */
    @Override
    protected String getBusinessColumn() {
        return "menu_code";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyMenuPo entity) {
        return entity.getMenuCode();
    }

    /**
     * Insert a new aggregate.
     *
     * @param menu aggregate
     */
    @Override
    public void insert(JulyMenu menu) {
        insert(toPo(menu));
    }

    /**
     * Update an existing aggregate.
     *
     * @param menu aggregate with id
     */
    @Override
    public void update(JulyMenu menu) {
        update(toPo(menu));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyMenu findById(String id) {
        JulyMenuPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by the unique menu code.
     *
     * @param menuCode menu code
     * @return aggregate or null
     */
    @Override
    public JulyMenu findByCode(String menuCode) {
        JulyMenuPo po = getByBusinessValue(menuCode);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Whether a menu has alive children.
     *
     * @param id menu id
     * @return true when children exist
     */
    @Override
    public boolean hasChildren(String id) {
        QueryWrapper<JulyMenuPo> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);

        return mapper.selectCount(wrapper) > 0;
    }

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        JulyMenuPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /**
     * Load the full alive menu tree (children assembled by the tree base).
     *
     * @return root nodes with nested children
     */
    @Override
    public List<JulyMenu> selectMenuTree() {
        return toAggregateTree(super.selectTree());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Menu tree orders by sort_order first, then id.</p>
     */
    @Override
    protected com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<JulyMenuPo> treeWrapper() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<JulyMenuPo> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper.orderByAsc("sort_order").orderByAsc("id");

        return wrapper;
    }

    /**
     * Find aggregates by a id list (flat, no children assembly).
     *
     * @param ids menu ids
     * @return aggregates present in the store
     */
    @Override
    public List<JulyMenu> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        return mapper.selectByIds(ids).stream().map(this::toAggregate).toList();
    }

    /**
     * Offset based page query with an optional keyword filter.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param keyword  menu code / name keyword, nullable
     * @return page rows
     */
    @Override
    public List<JulyMenu> findPage(int offset, int pageSize, String keyword) {
        int current = offset / pageSize + 1;
        Page<JulyMenuPo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, keywordWrapper(keyword)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Count with the same filter as findPage.
     *
     * @param keyword menu code / name keyword, nullable
     * @return total row count
     */
    @Override
    public long count(String keyword) {
        return mapper.selectCount(keywordWrapper(keyword));
    }

    /**
     * Keyword filter wrapper shared by findPage and count.
     *
     * @param keyword menu code / name keyword, nullable
     * @return query wrapper
     */
    private QueryWrapper<JulyMenuPo> keywordWrapper(String keyword) {
        QueryWrapper<JulyMenuPo> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("menu_code", keyword).or().like("menu_name", keyword);
        }

        return wrapper;
    }

    /**
     * Map a Po tree to the aggregate tree.
     *
     * @param roots root Po nodes with nested children
     * @return root aggregates with nested children
     */
    private List<JulyMenu> toAggregateTree(List<JulyMenuPo> roots) {
        List<JulyMenu> result = new ArrayList<>();

        for (JulyMenuPo root : roots) {
            result.add(toAggregateRecursive(root));
        }

        return result;
    }

    /**
     * Recursive Po → aggregate mapping keeping the children tree.
     *
     * @param po Po node
     * @return aggregate node with children
     */
    private JulyMenu toAggregateRecursive(JulyMenuPo po) {
        JulyMenu menu = toAggregate(po);

        for (JulyMenuPo child : po.getChildren()) {
            menu.addChild(toAggregateRecursive(child));
        }

        return menu;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param menu aggregate
     * @return PO
     */
    private JulyMenuPo toPo(JulyMenu menu) {
        JulyMenuPo po = new JulyMenuPo();
        po.setId(menu.id().value());
        po.setMenuCode(menu.menuCode());
        po.setMenuName(menu.menuName());
        po.setMenuType(menu.menuType());
        po.setMenuIcon(menu.menuIcon());
        po.setMenuRoute(menu.menuRoute());
        po.setPermissionCode(menu.permissionCode());
        po.setComponent(menu.component());
        po.setParentId(menu.parentId());
        po.setSortOrder(menu.sortOrder());
        po.setStatus(menu.status());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyMenu toAggregate(JulyMenuPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyMenu(EntityId.of(po.getId()), po.getMenuCode(), po.getMenuName(), po.getMenuType(),
                po.getMenuIcon(), po.getMenuRoute(), po.getPermissionCode(), po.getComponent(), po.getParentId(),
                po.getSortOrder(), po.getStatus(), audit);
    }
}
