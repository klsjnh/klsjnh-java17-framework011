package com.klsjnh.application.system011.dictionary;

/*                JulyDictionaryUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary use case class
 *      2026.09.22  import workbook (xlsx upsert + replace children)
 *      2026.09.26  explicit permission checks (julyDictionary auth)
 *      2026.09.26  @Lazy ImportUseCase — break ImportProviderRegistry cycle
 *      2026.09.26  workbook import extracted to JulyDictionaryImportSupport
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.application.platform011.export.ExportUseCase;
import com.klsjnh.application.platform011.importdata.ImportUseCase;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.domain.platform011.importdata.ImportBundle;
import com.klsjnh.domain.platform011.importdata.ImportResult;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.dictionary.JulyDictionary;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryBundle;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItem;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItemRepository;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryQuerySpec;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryPermissionCodes011;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryRepository;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * JulyDictionary use cases: dictionary and item CRUD, plus the program read
 * entries getByType / getLabel / isValidItem (no permission check, no cache).
 * Management actions assert permission codes via {@link AuthorizationPort}.
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
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * Platform export use case.
     */
    private final ExportUseCase exportUseCase;

    /**
     * Platform import use case.
     */
    private final ImportUseCase importUseCase;

    /**
     * Workbook import support (xlsx upsert + replace children).
     */
    private final JulyDictionaryImportSupport importSupport;

    /**
     * Create the use case.
     *
     * @param repository          dictionary repository
     * @param itemRepository      dictionary item repository
     * @param authorizationPort   authorization port
     * @param exportUseCase       export use case
     * @param importUseCase       import use case
     * @param importSupport       workbook import support
     */
    public JulyDictionaryUseCase(JulyDictionaryRepository repository, JulyDictionaryItemRepository itemRepository,
            AuthorizationPort authorizationPort, ExportUseCase exportUseCase, @Lazy ImportUseCase importUseCase,
            JulyDictionaryImportSupport importSupport) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.authorizationPort = authorizationPort;
        this.exportUseCase = exportUseCase;
        this.importUseCase = importUseCase;
        this.importSupport = importSupport;
    }

    /**
     * Insert a new dictionary type.
     *
     * @param dictionaryCode dictionary code, unique, immutable
     * @param sortOrder      manual sort order, null falls back to the default
     * @param dictionaryName dictionary display name
     * @param status         row status, null falls back to enabled
     * @param remark         remark, optional
     * @return new dictionary id
     */
    @Transactional
    public String insert(String operatorId, String dictionaryCode, Integer sortOrder, String dictionaryName,
            String status, String remark) {
        authorizationPort.assertHas(operatorId, JulyDictionaryPermissionCodes011.INSERT);

        if (repository.findByCode(dictionaryCode) != null) {
            throw BusinessException.badRequest("dictionary code already exists: " + dictionaryCode);
        }

        requireStatus(status);

        JulyDictionary dictionary = newDictionary(dictionaryCode, sortOrder, dictionaryName, status, remark);
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
    public String update(String operatorId, String id, String dictionaryName, Integer sortOrder, String status,
            String remark) {
        authorizationPort.assertHas(operatorId, JulyDictionaryPermissionCodes011.UPDATE);
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
    public String logicDelete(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyDictionaryPermissionCodes011.LOGIC_DELETE);
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
    public JulyDictionary getById(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyDictionaryPermissionCodes011.SELECT);
        return require(id);
    }

    /**
     * Find a dictionary by primary key together with all its items.
     *
     * @param id dictionary id
     * @return bundle
     */
    public JulyDictionaryBundle getByIdWithItems(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyDictionaryPermissionCodes011.SELECT);
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
    public PageResult011<JulyDictionary> selectListByPage(String operatorId, PageQuery011 pageQuery,
            JulyDictionaryQuerySpec spec) {
        authorizationPort.assertHas(operatorId, JulyDictionaryPermissionCodes011.SELECT);
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
     * @param status         row status, null falls back to enabled
     * @param remark         remark, optional
     * @return new item id
     */
    @Transactional
    public String insertItem(String operatorId, String dictionaryCode, Integer sortOrder, String itemCode,
            String itemLabel, String status, String remark) {
        authorizationPort.assertHas(operatorId, JulyDictionaryPermissionCodes011.INSERT);
        JulyDictionary dictionary = repository.findByCode(dictionaryCode);

        if (dictionary == null) {
            throw BusinessException.recordNotFound(dictionaryCode);
        }

        String dictionaryId = dictionary.id().value();

        if (itemRepository.findByMasterAndCode(dictionaryId, itemCode) != null) {
            throw BusinessException.badRequest("item code already exists in dictionary: " + itemCode);
        }

        requireStatus(status);

        JulyDictionaryItem item = newItem(dictionaryId, sortOrder, itemCode, itemLabel, status, remark);
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
    public String updateItem(String operatorId, String id, String itemLabel, Integer sortOrder, String status,
            String remark) {
        authorizationPort.assertHas(operatorId, JulyDictionaryPermissionCodes011.UPDATE);
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
    public String logicDeleteItem(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyDictionaryPermissionCodes011.LOGIC_DELETE);
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
    public List<JulyDictionaryItem> selectItemListByType(String operatorId, String dictionaryCode, String status) {
        authorizationPort.assertHas(operatorId, JulyDictionaryPermissionCodes011.SELECT);
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
     * Export all dictionary rows (permission-gated).
     *
     * @param operator authenticated operator
     * @return export result
     */
    public ExportResult export(Operator011 operator) {
        requireOperator(operator);
        authorizationPort.assertHas(operator.id(), JulyDictionaryPermissionCodes011.EXPORT);
        return exportUseCase.export(AuditObjectCodes011.JULY_DICTIONARY, operator);
    }

    /**
     * Export all dictionary rows as an xlsx workbook (permission-gated).
     *
     * @param operator authenticated operator
     * @return xlsx bytes
     */
    public byte[] exportXlsx(Operator011 operator) {
        requireOperator(operator);
        authorizationPort.assertHas(operator.id(), JulyDictionaryPermissionCodes011.EXPORT);
        return exportUseCase.exportXlsx(AuditObjectCodes011.JULY_DICTIONARY, operator);
    }

    /**
     * Import an xlsx workbook (permission-gated).
     *
     * @param bytes    xlsx bytes
     * @param operator authenticated operator
     * @return import result
     */
    public ImportResult importXlsx(byte[] bytes, Operator011 operator) {
        requireOperator(operator);
        authorizationPort.assertHas(operator.id(), JulyDictionaryPermissionCodes011.IMPORT);
        return importUseCase.importXlsx(AuditObjectCodes011.JULY_DICTIONARY, bytes, operator);
    }

    /**
     * Import a workbook bundle: upsert masters by dictionaryCode; for every
     * code that appears on the master sheet, Replace that master's items from
     * the children sheet (other dictionaries untouched).
     *
     * @param bundle decoded sheets
     * @return import counts
     */
    @Transactional
    public ImportResult importWorkbook(ImportBundle bundle) {
        return importSupport.importWorkbook(bundle);
    }

    /**
     * Require an authenticated operator.
     *
     * @param operator operator
     */
    private void requireOperator(Operator011 operator) {
        if (operator == null || !operator.authenticated()) {
            throw BusinessException.unauthorized("not authenticated");
        }
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
     * @param status         row status
     * @param remark         remark
     * @return new aggregate
     */
    private JulyDictionary newDictionary(String dictionaryCode, Integer sortOrder, String dictionaryName, String status,
            String remark) {
        try {
            return JulyDictionary.create(EntityId.generate(), dictionaryCode, sortOrder, dictionaryName, status, remark,
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
     * @param status       row status
     * @param remark       remark
     * @return new entity
     */
    private JulyDictionaryItem newItem(String dictionaryId, Integer sortOrder, String itemCode, String itemLabel,
            String status, String remark) {
        try {
            return JulyDictionaryItem.create(EntityId.generate(), dictionaryId, sortOrder, itemCode, itemLabel, status,
                    remark, AuditInfo.empty());
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
