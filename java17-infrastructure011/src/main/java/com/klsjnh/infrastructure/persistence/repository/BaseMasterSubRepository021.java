package com.klsjnh.infrastructure.persistence.repository;

/*                BaseMasterSubRepository021 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  base master sub repository 021 class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.infrastructure.persistence.entity.BasePo;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Master-sub repository base (tier 021): cascade behaviour for master tables
 * that own child tables — whole save (master + children), query with children
 * and cascade logical delete.
 * <p>
 * Subclasses implement {@link #getChildServices()} to declare the child
 * repositories; each child PO implements {@link MasterLinked} (the fixed
 * {@code pk_mt} master link column). Children of a master-sub aggregate share
 * the master lifecycle — they are components, not independent aggregates. The
 * calling application use case owns the transaction: cascade writes are only
 * safe inside one.
 * </p>
 * <p>
 * Both shapes are supported: one-master-one-child (a single-entry children
 * map) and one-master-multiple-children (multiple entries).
 * </p>
 *
 * @param <T> master PO type (extends {@link BasePo})
 * @param <M> master mapper type
 */

public abstract class BaseMasterSubRepository021<T extends BasePo, M extends BaseMapper<T>>
        extends BaseRepository<T, M> {

    /**
     * Create the master-sub repository base.
     *
     * @param mapper       master mapper
     * @param commonMapper native sql mapper
     */
    protected BaseMasterSubRepository021(M mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Child repositories owned by this master.
     *
     * @return list of child repositories
     */
    protected abstract List<BaseRepository<?, ?>> getChildServices();

    /**
     * Save the master row only.
     *
     * @param master master entity (with id)
     * @return saved master
     */
    public T saveMaster(T master) {
        String funcName = "save master";

        if (master == null || master.getId() == null || master.getId().isBlank()) {
            throw BusinessException.badRequest(funcName + ": master id is required");
        }

        update(master);

        return getById(master.getId());
    }

    /**
     * Save a single child list under the master: old children are logically
     * deleted, then the given list is inserted (replace strategy).
     *
     * @param masterId     master id
     * @param childService the child repository
     * @param children     child entities
     * @param <C>          child PO type
     */
    public <C extends BasePo & MasterLinked> void saveChildren(String masterId,
            BaseRepository<C, ?> childService, List<C> children) {
        String funcName = "save children";

        if (masterId == null || masterId.isBlank()) {
            throw BusinessException.badRequest(funcName + ": master id is required");
        }

        deleteChildrenByMaster(masterId, childService);

        if (children == null || children.isEmpty()) {
            return;
        }

        for (C child : children) {
            child.setPkMt(masterId);
            childService.insert(child);
        }
    }

    /**
     * Save master and all children together (cascade whole save).
     *
     * @param master            master entity
     * @param childrenByService child data keyed by child repository
     * @return saved master
     */
    public T saveWhole(T master, Map<BaseRepository<?, ?>, List<? extends BasePo>> childrenByService) {
        String funcName = "save whole";

        if (master == null) {
            throw BusinessException.badRequest(funcName + ": master is required");
        }

        if (master.getId() == null || master.getId().isBlank()) {
            insert(master);
        } else {
            update(master);
        }

        if (childrenByService == null) {
            return master;
        }

        for (Map.Entry<BaseRepository<?, ?>, List<? extends BasePo>> entry : childrenByService.entrySet()) {
            saveChildrenRaw(master.getId(), entry.getKey(), entry.getValue());
        }

        return master;
    }

    /**
     * Raw-type bridge for the wildcard map in {@link #saveWhole}: replace old
     * children, then insert the given list with the master link set. Children
     * not implementing {@link MasterLinked} are rejected loudly instead of
     * being inserted with a blank master link.
     *
     * @param masterId     master id
     * @param childService the child repository
     * @param children     child entities
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void saveChildrenRaw(String masterId, BaseRepository childService, List children) {
        String funcName = "save children";

        deleteChildrenByMaster(masterId, childService);

        for (Object child : children) {
            if (!(child instanceof MasterLinked linked)) {
                throw BusinessException.badRequest(funcName + ": child must implement MasterLinked");
            }

            linked.setPkMt(masterId);
            childService.insert((BasePo) child);
        }
    }

    /**
     * Query the master together with all its children.
     *
     * @param id master id
     * @return map with "master" and child lists keyed by child key
     */
    public Map<String, Object> getWithChildren(String id) {
        String funcName = "get with children";

        if (id == null || id.isBlank()) {
            throw BusinessException.badRequest(funcName + ": master id is required");
        }

        T master = getById(id);

        if (master == null) {
            throw BusinessException.notFound("record not found, id=" + id);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("master", master);

        for (BaseRepository<?, ?> childService : getChildServices()) {
            List<?> children = selectChildrenByMaster(id, childService);
            result.put(childKey(childService), children);
        }

        return result;
    }

    /**
     * Cascade logical delete: all children first, then the master.
     *
     * @param id master id
     * @return deleted master id
     */
    public String cascadeDelete(String id) {
        String funcName = "cascade delete";

        if (id == null || id.isBlank()) {
            throw BusinessException.badRequest(funcName + ": master id is required");
        }

        for (BaseRepository<?, ?> childService : getChildServices()) {
            deleteChildrenByMaster(id, childService);
        }

        T master = getById(id);

        if (master == null) {
            throw BusinessException.notFound("record not found, id=" + id);
        }

        return logicDelete(master);
    }

    /**
     * Delete children by master id (logical delete).
     *
     * @param masterId     master id
     * @param childService the child repository
     * @param <C>          child PO type
     */
    protected <C extends BasePo> void deleteChildrenByMaster(String masterId, BaseRepository<C, ?> childService) {
        for (C child : selectChildrenByMaster(masterId, childService)) {
            childService.logicDelete(child);
        }
    }

    /**
     * Select children by master id (the alive filter comes from
     * {@code @TableLogic} automatically).
     *
     * @param masterId     master id
     * @param childService the child repository
     * @param <C>          child PO type
     * @return matching children
     */
    protected <C extends BasePo> List<C> selectChildrenByMaster(String masterId, BaseRepository<C, ?> childService) {
        QueryWrapper<C> w = new QueryWrapper<>();
        w.eq(masterIdColumnFor(childService), masterId);

        return childService.selectList(w);
    }

    /**
     * Resolve the master link column for a child repository. Framework-fixed
     * to {@code pk_mt}; subclasses rarely override.
     *
     * @param childService the child repository
     * @return master link column name
     */
    protected String masterIdColumnFor(BaseRepository<?, ?> childService) {
        return "pk_mt";
    }

    /**
     * Child repository to response key mapping: the child repository class
     * simple name minus the RepositoryImpl suffix, decapitalized.
     *
     * @param childService the child repository
     * @return response key
     */
    protected String childKey(BaseRepository<?, ?> childService) {
        String name = childService.getClass().getSimpleName().replace("RepositoryImpl", "");

        return decapitalize(name);
    }

    /**
     * Lowercase the first char.
     *
     * @param name input string
     * @return string with lowercased first char
     */
    private String decapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
