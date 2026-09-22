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
 * Master-sub repository base: behaviour for master tables that own child
 * tables. The cascade rules live in {@link MasterSubSupport}.
 * <p>
 * <b>Three save / read granularities are first-class</b>. The master axis
 * ({@code pk_mt}) is deliberately orthogonal to the tree axis
 * ({@code parent_id} / {@code selectTree}) — a tree master-sub calls both, it
 * does not get a merged "save the whole tree" method:
 * </p>
 * <pre>
 * grain           write                    read                        delete
 * master only     saveMaster               getMaster (= getById)       logicDelete (master only)
 * children only   saveChildren (REPLACE)   getChildren                 child repository logicDelete
 * master + child  saveWhole                getWithChildren             cascadeDelete (children, then master)
 * </pre>
 * <p>
 * Subclasses implement {@link #getChildServices()} to declare the child
 * repositories; each child PO implements {@link MasterLinked} (the fixed
 * {@code pk_mt} master link column). Children of a master-sub aggregate share
 * the master lifecycle — they are components, not independent aggregates. The
 * calling application use case owns the transaction: cascade writes are only
 * safe inside one.
 * </p>
 * <p>
 * <b>Lifecycle:</b> the caller generates the primary key
 * ({@code EntityId.generate()}) before saving — the blank-id branch of
 * {@link #saveMaster} / {@link #saveWhole} is only the {@code ASSIGN_UUID}
 * fallback, not the normal path.
 * </p>
 * <p>
 * <b>Registration ≠ mandatory cascade:</b> only the children returned by
 * {@link #getChildServices()} take part in {@link #getWithChildren} /
 * {@link #cascadeDelete}. Registering a child grants the capability; whether a
 * delete is allowed stays a use-case guard, which may refuse instead of
 * cascading. Append-only audit tables and toggle-dr link tables are
 * structurally children but deliberately not registered.
 * </p>
 * <p>
 * The type bound is {@link BasePo}, so this base serves both unsorted
 * ({@code BasePo}) and sorted ({@code BasePo011}) master tables; the 011 suffix
 * labels the repository base family, not a sort-order requirement.
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
     * Save the master row only (never touches children): a blank id inserts, a
     * present id updates.
     * <p>
     * The caller generates the id ({@code EntityId.generate()}) before saving —
     * the blank-id branch is only the {@code ASSIGN_UUID} fallback.
     * </p>
     *
     * @param master master entity
     * @return saved master, re-read
     */
    public T saveMaster(T master) {
        String funcName = "save master";

        if (master == null) {
            throw BusinessException.badRequest(funcName + ": master is required");
        }

        if (master.getId() == null || master.getId().isBlank()) {
            insert(master);
        } else {
            update(master);
        }

        return getById(master.getId());
    }

    /**
     * Get the master by id — an alias of {@link #getById(String)}, named to
     * mirror {@link #saveMaster}.
     *
     * @param id master id
     * @return master or null
     */
    public T getMaster(String id) {
        return getById(id);
    }

    /**
     * Save a single child list under the master — <b>Replace strategy</b>: the
     * old children of the master are logically deleted first, then the given
     * list is inserted.
     * <p>
     * For incremental child editing (add / update / delete one row at a time,
     * e.g. dictionary items) call the child repository directly
     * ({@code insert} / {@code update} / {@code logicDelete}) — do <b>not</b>
     * route row-by-row edits through this method.
     * </p>
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
     * Read the children of one master from a given child repository — the
     * read-side counterpart of {@link #saveChildren} for a single child table.
     *
     * @param masterId     master id
     * @param childService the child repository
     * @param <C>          child PO type
     * @return matching children
     */
    public <C extends BasePo> List<C> getChildren(String masterId, BaseRepository<C, ?> childService) {
        return MasterSubSupport.selectChildrenByMaster(masterId, childService);
    }

    /**
     * Save master and all children together: the master is inserted on a blank
     * id and updated otherwise, then every child list is replaced
     * (<b>Replace strategy</b>, per {@link #saveChildren}).
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
     * Query the master together with the registered children.
     * <p>
     * Map keys: {@code "master"} plus, per child service, the key derived by
     * {@link MasterSubSupport#childKey} — the child repository class simple
     * name minus the {@code RepositoryImpl} suffix, first letter lowercased
     * (e.g. {@code JulyDictionaryItemRepositoryImpl} →
     * {@code julyDictionaryItem}). Renaming a child repository therefore
     * changes this contract.
     * </p>
     *
     * @param id master id
     * @return map with {@code "master"} and the child lists by child key
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
     * Cascade logical delete of the <b>registered</b> children first, then the
     * master.
     * <p>
     * Capability, not obligation: a business guard may refuse the delete
     * instead (see the use-case guards, e.g. the dictionary "still has items"
     * rule) — the base never forces a cascade.
     * </p>
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
