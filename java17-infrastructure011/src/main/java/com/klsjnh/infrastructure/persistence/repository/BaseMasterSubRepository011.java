package com.klsjnh.infrastructure.persistence.repository;

/*                BaseMasterSubRepository011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  base master sub repository class
 *      2026.09.21  renamed from BaseMasterSubRepository021; cascade rules extracted to MasterSubSupport
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.infrastructure.persistence.entity.BasePo;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.support.MasterSubSupport;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Master-sub repository base: cascade behaviour for master tables that own
 * child tables — whole save (master + children), query with children and
 * cascade logical delete. The cascade rules live in {@link MasterSubSupport}.
 * <p>
 * Subclasses implement {@link #getChildServices()} to declare the child
 * repositories; each child PO implements {@link MasterLinked} (the fixed
 * {@code pk_mt} master link column). Children of a master-sub aggregate share
 * the master lifecycle — they are components, not independent aggregates. The
 * calling application use case owns the transaction: cascade writes are only
 * safe inside one.
 * </p>
 * <p>
 * The type bound is {@link BasePo}, so this base serves both unsorted
 * ({@code BasePo}) and sorted ({@code BasePo011}) master tables; the 011 suffix
 * labels the repository base family, not a sort-order requirement. Both shapes
 * are supported: one-master-one-child (a single-entry children map) and
 * one-master-multiple-children (multiple entries).
 * </p>
 *
 * @param <T> master PO type (extends {@link BasePo})
 * @param <M> master mapper type
 */

public abstract class BaseMasterSubRepository011<T extends BasePo, M extends BaseMapper<T>>
        extends BaseRepository<T, M> {

    /**
     * Create the master-sub repository base.
     *
     * @param mapper       master mapper
     * @param commonMapper native sql mapper
     */
    protected BaseMasterSubRepository011(M mapper, CommonMapper commonMapper) {
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
        MasterSubSupport.saveChildren(masterId, childService, children);
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
            MasterSubSupport.saveChildrenRaw(master.getId(), entry.getKey(), entry.getValue());
        }

        return master;
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
            throw BusinessException.recordNotFound(id);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("master", master);

        for (BaseRepository<?, ?> childService : getChildServices()) {
            List<?> children = MasterSubSupport.selectChildrenByMaster(id, childService);
            result.put(MasterSubSupport.childKey(childService), children);
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
            MasterSubSupport.deleteChildrenByMaster(id, childService);
        }

        T master = getById(id);

        if (master == null) {
            throw BusinessException.recordNotFound(id);
        }

        return logicDelete(master);
    }
}
