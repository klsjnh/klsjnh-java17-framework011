package com.klsjnh.application.lowcode011;

/*                JulyMetadataPublishUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata publish use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataRepository;
import com.klsjnh.domain.lowcode011.JulyMetadataVersion;
import com.klsjnh.domain.lowcode011.JulyMetadataVersionRepository;
import com.klsjnh.domain.lowcode011.MetadataDdlExecutorPort;
import com.klsjnh.domain.lowcode011.MetadataDdlGeneratorPort;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Publish use case (phase 2): turns object metadata into a physical table.
 * The first publish creates with {@code CREATE TABLE IF NOT EXISTS}; later
 * publishes only {@code ALTER ... ADD COLUMN} the fields that are absent — never
 * DROP / MODIFY / RENAME. A snapshot is written and the object's publish
 * pointer is advanced.
 */

@Service
public class JulyMetadataPublishUseCase {

    /**
     * Physical table prefix.
     */
    private static final String TABLE_PREFIX = "lc_";

    /**
     * First published version.
     */
    private static final String FIRST_VERSION = "0.0.1";

    /**
     * Metadata CRUD use case.
     */
    private final JulyMetadataUseCase metadataUseCase;

    /**
     * Metadata repository (publish pointer).
     */
    private final JulyMetadataRepository metadataRepository;

    /**
     * Snapshot repository.
     */
    private final JulyMetadataVersionRepository versionRepository;

    /**
     * DDL generator.
     */
    private final MetadataDdlGeneratorPort ddlGenerator;

    /**
     * DDL executor.
     */
    private final MetadataDdlExecutorPort ddlExecutor;

    /**
     * JSON mapper for the snapshot payload.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create the use case.
     *
     * @param metadataUseCase   metadata CRUD use case
     * @param metadataRepository metadata repository
     * @param versionRepository  snapshot repository
     * @param ddlGenerator       DDL generator
     * @param ddlExecutor        DDL executor
     */
    public JulyMetadataPublishUseCase(JulyMetadataUseCase metadataUseCase, JulyMetadataRepository metadataRepository,
            JulyMetadataVersionRepository versionRepository, MetadataDdlGeneratorPort ddlGenerator,
            MetadataDdlExecutorPort ddlExecutor) {
        this.metadataUseCase = metadataUseCase;
        this.metadataRepository = metadataRepository;
        this.versionRepository = versionRepository;
        this.ddlGenerator = ddlGenerator;
        this.ddlExecutor = ddlExecutor;
    }

    /**
     * Publish an object: create or alter its physical table, snapshot, advance
     * the publish pointer.
     *
     * @param objectName     object name
     * @param migrateData    reserved for the data migration step
     * @param includeDeleted reserved for the data migration step
     * @return publish result
     */
    public Map<String, Object> publish(String objectName, boolean migrateData, boolean includeDeleted) {
        JulyMetadata metadata = metadataUseCase.getByObjectName(objectName);
        String table = TABLE_PREFIX + metadata.objectName();
        String version = nextVersion(versionRepository.findLatest(objectName));
        String ddl = buildDdl(table, metadata);

        if (ddl != null) {
            try {
                ddlExecutor.execute(ddl);
            } catch (IllegalStateException ex) {
                throw BusinessException.badRequest(ex.getMessage());
            } catch (RuntimeException ex) {
                throw BusinessException.badRequest("publish ddl failed: " + ex.getMessage());
            }
        }

        versionRepository.insert(new JulyMetadataVersion(EntityId.generate().value(), objectName, version,
                toPayload(metadata), table, null, ddl, "PUBLISHED", null, LocalDateTime.now()));
        metadataRepository.updatePublishState(metadata.id().value(), "PUBLISHED", version, table, null);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("objectName", objectName);
        result.put("version", version);
        result.put("publishStatus", "published");
        result.put("physicalTable", table);
        result.put("backupTable", null);
        result.put("ddl", ddl);

        return result;
    }

    /**
     * Build the DDL: CREATE when the table is missing, otherwise ADD only the
     * missing columns; null when there is nothing to do.
     *
     * @param table    physical table
     * @param metadata aggregate
     * @return ddl or null
     */
    private String buildDdl(String table, JulyMetadata metadata) {
        try {
            if (!ddlExecutor.tableExists(table)) {
                return ddlGenerator.generateCreate(table, metadata.description(), metadata.fields(),
                        metadata.businessField());
            }

            Set<String> existing = ddlExecutor.columnsOf(table);
            List<JulyMetadataField> missing = metadata.fields().stream()
                    .filter(field -> !existing.contains(field.fieldCode().toLowerCase(Locale.ROOT)))
                    .toList();

            return missing.isEmpty() ? null : ddlGenerator.generateAddColumns(table, missing);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Next version: 0.0.1 on first publish, otherwise patch + 1.
     *
     * @param latest latest snapshot, nullable
     * @return next version
     */
    private String nextVersion(JulyMetadataVersion latest) {
        if (latest == null || latest.version() == null || latest.version().isBlank()) {
            return FIRST_VERSION;
        }

        String[] parts = latest.version().split("\\.");

        try {
            int patch = Integer.parseInt(parts[parts.length - 1]);
            parts[parts.length - 1] = String.valueOf(patch + 1);

            return String.join(".", parts);
        } catch (NumberFormatException ex) {
            return latest.version() + ".1";
        }
    }

    /**
     * Serialize the aggregate as the snapshot payload.
     *
     * @param metadata aggregate
     * @return json text
     */
    private String toPayload(JulyMetadata metadata) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("objectName", metadata.objectName());
        payload.put("objectType", metadata.objectType());
        payload.put("description", metadata.description());
        payload.put("businessField", metadata.businessField());
        payload.put("routerPath", metadata.routerPath());
        payload.put("fields", metadata.fields());
        payload.put("displays", metadata.displays());
        payload.put("services", metadata.services());

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw BusinessException.badRequest("snapshot serialize failed: " + ex.getMessage());
        }
    }
}
