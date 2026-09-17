package com.klsjnh.application.lowcode011;

/*                JulyMetadataDataSyncUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata data sync use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.application.dataservice011.BusinessModelingExecuteCommand;
import com.klsjnh.application.dataservice011.JulyBusinessModelingUseCase;
import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataRepository;
import com.klsjnh.domain.lowcode011.JulyMetadataVersion;
import com.klsjnh.domain.lowcode011.JulyMetadataVersionRepository;
import com.klsjnh.domain.lowcode011.MetadataDataWriterPort;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Data sync use case: pages the source SQL through the business modeling
 * executor (dialect paging) and upserts the rows into the published physical
 * table. First run initializes, later runs sync.
 */

@Service
public class JulyMetadataDataSyncUseCase {

    /**
     * Default page size.
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * Max page size.
     */
    private static final int MAX_PAGE_SIZE = 500;

    /**
     * Metadata CRUD use case.
     */
    private final JulyMetadataUseCase metadataUseCase;

    /**
     * Metadata repository (sync state).
     */
    private final JulyMetadataRepository metadataRepository;

    /**
     * Snapshot repository (physical table).
     */
    private final JulyMetadataVersionRepository versionRepository;

    /**
     * Data writer.
     */
    private final MetadataDataWriterPort dataWriter;

    /**
     * Business modeling executor (source read + dialect paging).
     */
    private final JulyBusinessModelingUseCase businessModelingUseCase;

    /**
     * Metadata-driven value validator (sync writes must pass the same gate as
     * runtime writes).
     */
    private final MetadataValueValidator valueValidator;

    /**
     * Create the use case.
     *
     * @param metadataUseCase        metadata CRUD use case
     * @param metadataRepository     metadata repository
     * @param versionRepository      snapshot repository
     * @param dataWriter             data writer
     * @param businessModelingUseCase business modeling executor
     * @param valueValidator         metadata value validator
     */
    public JulyMetadataDataSyncUseCase(JulyMetadataUseCase metadataUseCase,
            JulyMetadataRepository metadataRepository, JulyMetadataVersionRepository versionRepository,
            MetadataDataWriterPort dataWriter, JulyBusinessModelingUseCase businessModelingUseCase,
            MetadataValueValidator valueValidator) {
        this.metadataUseCase = metadataUseCase;
        this.metadataRepository = metadataRepository;
        this.versionRepository = versionRepository;
        this.dataWriter = dataWriter;
        this.businessModelingUseCase = businessModelingUseCase;
        this.valueValidator = valueValidator;
    }

    /**
     * Import one page of source data into the published physical table.
     *
     * @param objectName     object name
     * @param dataSourceCode source datasource code
     * @param sqlCode        source sql
     * @param pageNum        page number, 1 based
     * @param pageSize       page size
     * @param forceInit      force initialize mode
     * @return import result
     */
    public Map<String, Object> importDataFromSql(String objectName, String dataSourceCode, String sqlCode,
            Integer pageNum, Integer pageSize, Boolean forceInit) {
        JulyMetadata metadata = metadataUseCase.getByObjectName(objectName);
        JulyMetadataVersion latest = versionRepository.findLatest(objectName);

        if (latest == null) {
            throw BusinessException.badRequest("object not published: " + objectName);
        }

        if (dataSourceCode == null || dataSourceCode.isBlank()) {
            throw BusinessException.badRequest("dataSourceCode required");
        }

        if (sqlCode == null || sqlCode.isBlank()) {
            throw BusinessException.badRequest("sqlCode required");
        }

        int page = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        PageResult011<Map<String, Object>> source = businessModelingUseCase.executeSqlByPage(
                new BusinessModelingExecuteCommand(null, dataSourceCode, null, null, sqlCode), page, size);

        List<Map<String, Object>> rows = source.rows();
        List<String> errors = validateRows(rows, metadata);

        if (!errors.isEmpty()) {
            throw BusinessException.badRequest("sync validation failed: " + String.join("; ", errors));
        }

        int processed = dataWriter.upsert(latest.physicalTable(), metadata.businessField(), rows);
        boolean init = Boolean.TRUE.equals(forceInit) || !metadataRepository.isSynced(metadata.objectName());

        metadataRepository.markSynced(metadata.objectName());

        boolean hasMore = page < source.totalPages();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("objectName", objectName);
        result.put("mode", init ? "init" : "sync");
        result.put("pageNum", page);
        result.put("pageSize", size);
        result.put("hasMore", hasMore);
        result.put("nextPageNum", hasMore ? page + 1 : null);
        result.put("inserted", processed);
        result.put("updated", 0);
        result.put("unchanged", 0);
        result.put("skipped", 0);
        result.put("processed", processed);
        result.put("dataInitialized", true);

        return result;
    }

    /**
     * Validate source rows against the object metadata: the business field must
     * be present and every mapped value must satisfy its field type / length
     * (partial mode — base columns are filled by the writer).
     *
     * @param rows     source rows (labels may be upper case on Oracle)
     * @param metadata object metadata
     * @return errors (capped), empty when valid
     */
    private List<String> validateRows(List<Map<String, Object>> rows, JulyMetadata metadata) {
        List<String> errors = new ArrayList<>();
        String business = metadata.businessField() == null ? null
                : metadata.businessField().toLowerCase(Locale.ROOT);

        for (Map<String, Object> row : rows) {
            Map<String, Object> normalized = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                normalized.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue());
            }

            if (business != null && !normalized.containsKey(business)) {
                errors.add("business field missing: " + metadata.businessField());
            }

            errors.addAll(valueValidator.validate(normalized, metadata.fields(), true));

            if (errors.size() >= 20) {
                break;
            }
        }

        return errors;
    }

    /**
     * Import status of an object.
     *
     * @param objectName object name
     * @return import status
     */
    public Map<String, Object> importStatus(String objectName) {
        JulyMetadataVersion latest = versionRepository.findLatest(objectName);

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("objectName", objectName);
        status.put("dataInitialized", metadataRepository.isSynced(objectName));
        status.put("physicalTable", latest == null ? null : latest.physicalTable());
        status.put("publishStatus", latest == null ? "draft" : "published");

        return status;
    }
}
