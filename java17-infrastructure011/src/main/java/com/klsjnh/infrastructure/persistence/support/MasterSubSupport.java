package com.klsjnh.infrastructure.persistence.support;

/*                MasterSubSupport class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  master-sub cascade helper (extracted from BaseMasterSubRepository)
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.infrastructure.persistence.entity.BasePo;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

/**
 * Master-sub cascade helper: the save / select / delete rules for child tables
 * linked by the fixed {@code pk_mt} column. Shared by the master-sub and
 * tree-sub repository bases so the cascade rules live in one place.
 * <p>
 * The fixed link column is {@code pk_mt}; children must implement
 * {@link MasterLinked} so the cascade sets the link without reflection.
 * </p>
 */

public final class MasterSubSupport {

    /**
     * Fixed master link column.
     */
    public static final String MASTER_ID_COLUMN = "pk_mt";

    /**
     * Not instantiable.
     */
    private MasterSubSupport() {
    }

    /**
     * Save a single child list under the master — <b>Replace strategy</b>: old
     * children are logically deleted, then the given list is inserted.
     * <p>
     * Incremental editing is a different path: call the child repository
     * directly ({@code insert} / {@code update} / {@code logicDelete}) for
     * row-by-row adds / edits / deletes. Never route incremental edits through
     * this method — it would wipe the untouched rows.
     * </p>
     *
     * @param masterId     master id
     * @param childService the child repository
     * @param children     child entities
     * @param <C>          child PO type
     */
    public static <C extends BasePo & MasterLinked> void saveChildren(String masterId,
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
     * Raw-type bridge for the wildcard child map of the whole save: replace old
     * children, then insert the given list with the master link set. Children
     * not implementing {@link MasterLinked} are rejected loudly instead of
     * being inserted with a blank master link.
     *
     * @param masterId     master id
     * @param childService the child repository
     * @param children     child entities
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void saveChildrenRaw(String masterId, BaseRepository childService, List children) {
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
     * Select children by master id (the alive filter comes from
     * {@code @TableLogic} automatically).
     *
     * @param masterId     master id
     * @param childService the child repository
     * @param <C>          child PO type
     * @return matching children
     */
    public static <C extends BasePo> List<C> selectChildrenByMaster(String masterId,
            BaseRepository<C, ?> childService) {
        QueryWrapper<C> wrapper = new QueryWrapper<>();
        wrapper.eq(MASTER_ID_COLUMN, masterId);

        return childService.selectList(wrapper);
    }

    /**
     * Delete children by master id (logical delete).
     *
     * @param masterId     master id
     * @param childService the child repository
     * @param <C>          child PO type
     */
    public static <C extends BasePo> void deleteChildrenByMaster(String masterId,
            BaseRepository<C, ?> childService) {
        for (C child : selectChildrenByMaster(masterId, childService)) {
            childService.logicDelete(child);
        }
    }

    /**
     * Child repository to response key mapping: the child repository class
     * simple name minus the RepositoryImpl suffix, decapitalized.
     *
     * @param childService the child repository
     * @return response key
     */
    public static String childKey(BaseRepository<?, ?> childService) {
        String name = childService.getClass().getSimpleName().replace("RepositoryImpl", "");

        return decapitalize(name);
    }

    /**
     * Lowercase the first char.
     *
     * @param name input string
     * @return string with lowercased first char
     */
    private static String decapitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
