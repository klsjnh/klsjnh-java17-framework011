package com.klsjnh.application.system011.dictionary;

/*                JulyDictionaryUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary use case class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.dictionary.JulyDictionary;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryBundle;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItem;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItemRepository;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryQuerySpec;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JulyDictionary use cases: dictionary and item CRUD, plus the program read
 * entries getByType / getLabel / isValidItem (no HTTP endpoint, no cache).
 */

@Service
public class JulyDictionaryUseCase {

    /**
     * Dictionary repository.
     */
    private final JulyDictionaryRepository repository;

    /**
     * Dictionary item repository.
     */
    private final JulyDictionaryItemRepository itemRepository;

    /**
     * Create the use case.
     *
     * @param repository     dictionary repository
     * @param itemRepository dictionary item repository
     */
    public JulyDictionaryUseCase(JulyDictionaryRepository repository, JulyDictionaryItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    /**
     * Insert a new dictionary type.
     *
     * @param dictionaryCode dictionary code, unique, immutable
     * @param sortOrder      manual sort order, null falls back to the default
     * @param dictionaryName dictionary display name
     * @param remark         remark, optional
     * @return new dictionary id
     */
    @Transactional
    public String insert(String dictionaryCode, Integer sortOrder, String dictionaryName, String remark) {
        if (repository.findByCode(dictionaryCode) != null) {
            throw BusinessException.badRequest("dictionary code already exists: " + dictionaryCode);
        }

        JulyDictionary dictionary = newDictionary(dictionaryCode, sortOrder, dictionaryName, remark);
        repository.insert(dictionary);

        return dictionary.id().value();
    }

    /**
     * Update a dictionary type.
     *
     * @param id             dictionary id
     * @param dictionaryName dictionary display name
     * @param sortOrder      manual sort order, null keeps the stored one
     * @param status         row status, null keeps the stored one
     * @param remark         remark, optional
     * @return dictionary id
     */
    @Transactional
    public String update(String id, String dictionaryName, Integer sortOrder, String status, String remark) {
        JulyDictionary dictionary = require(id);
        requireStatus(status);
        applyUpdate(dictionary, dictionaryName, sortOrder, status, remark);
        repository.update(dictionary);

        return dictionary.id().value();
    }

    /**
     * Logic delete a dictionary; refused while it still has items.
     *
     * @param id dictionary id
     * @return deleted dictionary id
     */
    @Transactional
    public String logicDelete(String id) {
        JulyDictionary dictionary = require(id);

        if (itemRepository.countByMaster(dictionary.id().value()) > 0) {
            throw BusinessException.badRequest("dictionary still has items, delete them first: " + id);
        }

        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * Find a dictionary by primary key.
     *
     * @param id dictionary id
     * @return aggregate
     */
    public JulyDictionary getById(String id) {
        return require(id);
    }

    /**
     * Find a dictionary by primary key together with all its items.
     *
     * @param id dictionary id
     * @return bundle
     */
    public JulyDictionaryBundle getByIdWithItems(String id) {
        JulyDictionary dictionary = require(id);

        return new JulyDictionaryBundle(dictionary, itemRepository.findAllByMaster(dictionary.id().value(), null));
    }

    /**
     * Find a dictionary by code together with all its items (management read
     * entry; disabled rows included).
     *
     * @param dictionaryCode dictionary code
     * @return bundle
     */
    public JulyDictionaryBundle getByCodeWithItems(String dictionaryCode) {
        JulyDictionary dictionary = requireByCode(dictionaryCode);

        return new JulyDictionaryBundle(dictionary, itemRepository.findAllByMaster(dictionary.id().value(), null));
    }

    /**
     * Page query on the management view.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param spec      query condition, null means no filter
     * @return page result
     */
    public PageResult011<JulyDictionary> selectListByPage(PageQuery011 pageQuery, JulyDictionaryQuerySpec spec) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        JulyDictionaryQuerySpec condition = spec == null ? new JulyDictionaryQuerySpec(null, null) : spec;
        List<JulyDictionary> rows = repository.findPage(query.offset(), query.pageSize(), condition);
        long total = repository.count(condition);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Insert a new item under a dictionary.
     *
     * @param dictionaryCode dictionary code locating the master
     * @param sortOrder      manual sort order, null falls back to the default
     * @param itemCode       item code, unique within the dictionary
     * @param itemLabel      item display name
     * @param remark         remark, optional
     * @return new item id
     */
    @Transactional
    public String insertItem(String dictionaryCode, Integer sortOrder, String itemCode, String itemLabel, String remark) {
        JulyDictionary dictionary = repository.findByCode(dictionaryCode);

        if (dictionary == null) {
            throw BusinessException.recordNotFound(dictionaryCode);
        }

        String dictionaryId = dictionary.id().value();

        if (itemRepository.findByMasterAndCode(dictionaryId, itemCode) != null) {
            throw BusinessException.badRequest("item code already exists in dictionary: " + itemCode);
        }

        JulyDictionaryItem item = newItem(dictionaryId, sortOrder, itemCode, itemLabel, remark);
        itemRepository.insert(item);

        return item.id().value();
    }

    /**
     * Update an item.
     *
     * @param id        item id
     * @param itemLabel item display name
     * @param sortOrder manual sort order, null keeps the stored one
     * @param status    row status, null keeps the stored one
     * @param remark    remark, optional
     * @return item id
     */
    @Transactional
    public String updateItem(String id, String itemLabel, Integer sortOrder, String status, String remark) {
        JulyDictionaryItem item = requireItem(id);
        requireStatus(status);
        applyItemUpdate(item, itemLabel, sortOrder, status, remark);
        itemRepository.update(item);

        return item.id().value();
    }

    /**
     * Logic delete an item.
     *
     * @param id item id
     * @return deleted item id
     */
    @Transactional
    public String logicDeleteItem(String id) {
        if (!itemRepository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        return id;
    }

    /**
     * List the items of a dictionary (management view).
     *
     * @param dictionaryCode dictionary code
     * @param status         optional status filter, null for all
     * @return ordered entities, never null
     */
    public List<JulyDictionaryItem> selectItemListByType(String dictionaryCode, String status) {
        JulyDictionary dictionary = requireByCode(dictionaryCode);

        return itemRepository.findAllByMaster(dictionary.id().value(), status);
    }

    /**
     * Program read entry: the ENABLED dictionary of a code together with its
     * ENABLED items, null when missing or disabled. Readers query the store
     * every time (no cache).
     *
     * @param dictionaryCode dictionary code
     * @return bundle or null
     */
    public JulyDictionaryBundle getByType(String dictionaryCode) {
        JulyDictionary dictionary = repository.findEnabledByCode(dictionaryCode);

        if (dictionary == null) {
            return null;
        }

        return new JulyDictionaryBundle(dictionary, itemRepository.findByMaster(dictionary.id().value()));
    }

    /**
     * Program read entry: the label of an item code under a dictionary, null
     * when missing or disabled.
     *
     * @param dictionaryCode dictionary code
     * @param itemCode       item code
     * @return item label or null
     */
    public String getLabel(String dictionaryCode, String itemCode) {
        JulyDictionaryBundle bundle = getByType(dictionaryCode);

        if (bundle == null) {
            return null;
        }

        for (JulyDictionaryItem item : bundle.items()) {
            if (item.itemCode().equals(itemCode)) {
                return item.itemLabel();
            }
        }

        return null;
    }

    /**
     * Program read entry: whether an item code is a valid enabled value under a
     * dictionary.
     *
     * @param dictionaryCode dictionary code
     * @param itemCode       item code
     * @return true when the value is valid
     */
    public boolean isValidItem(String dictionaryCode, String itemCode) {
        return getLabel(dictionaryCode, itemCode) != null;
    }

    /**
     * Apply an update on the aggregate, translating domain validation failures
     * into 400 responses.
     *
     * @param dictionary     aggregate
     * @param dictionaryName dictionary display name
     * @param sortOrder      manual sort order
     * @param status         row status
     * @param remark         remark
     */
    private void applyUpdate(JulyDictionary dictionary, String dictionaryName, Integer sortOrder, String status,
            String remark) {
        try {
            dictionary.update(dictionaryName, sortOrder, status, remark);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Apply an update on the item entity, translating domain validation failures
     * into 400 responses.
     *
     * @param item      entity
     * @param itemLabel item display name
     * @param sortOrder manual sort order
     * @param status    row status
     * @param remark    remark
     */
    private void applyItemUpdate(JulyDictionaryItem item, String itemLabel, Integer sortOrder, String status,
            String remark) {
        try {
            item.update(itemLabel, sortOrder, status, remark);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Build a new dictionary aggregate, translating domain validation failures
     * into 400 responses.
     *
     * @param dictionaryCode dictionary code
     * @param sortOrder      manual sort order
     * @param dictionaryName dictionary display name
     * @param remark         remark
     * @return new aggregate
     */
    private JulyDictionary newDictionary(String dictionaryCode, Integer sortOrder, String dictionaryName, String remark) {
        try {
            return JulyDictionary.create(EntityId.generate(), dictionaryCode, sortOrder, dictionaryName, remark,
                    AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Build a new item entity, translating domain validation failures into 400
     * responses.
     *
     * @param dictionaryId dictionary id
     * @param sortOrder    manual sort order
     * @param itemCode     item code
     * @param itemLabel    item display name
     * @param remark       remark
     * @return new entity
     */
    private JulyDictionaryItem newItem(String dictionaryId, Integer sortOrder, String itemCode, String itemLabel,
            String remark) {
        try {
            return JulyDictionaryItem.create(EntityId.generate(), dictionaryId, sortOrder, itemCode, itemLabel, remark,
                    AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Require an existing dictionary by id.
     *
     * @param id dictionary id
     * @return aggregate
     */
    private JulyDictionary require(String id) {
        JulyDictionary dictionary = repository.findById(id);

        if (dictionary == null) {
            throw BusinessException.recordNotFound(id);
        }

        return dictionary;
    }

    /**
     * Require an existing dictionary by code (enabled or not).
     *
     * @param dictionaryCode dictionary code
     * @return aggregate
     */
    private JulyDictionary requireByCode(String dictionaryCode) {
        JulyDictionary dictionary = repository.findByCode(dictionaryCode);

        if (dictionary == null) {
            throw BusinessException.recordNotFound(dictionaryCode);
        }

        return dictionary;
    }

    /**
     * Require an existing item.
     *
     * @param id item id
     * @return entity
     */
    private JulyDictionaryItem requireItem(String id) {
        JulyDictionaryItem item = itemRepository.findById(id);

        if (item == null) {
            throw BusinessException.recordNotFound(id);
        }

        return item;
    }

    /**
     * Reject an unknown status when one is supplied.
     *
     * @param status raw status, nullable
     */
    private void requireStatus(String status) {
        if (status != null && !status.isBlank() && Status011.of(status) == null) {
            throw BusinessException.badRequest("unknown status: " + status);
        }
    }
}
