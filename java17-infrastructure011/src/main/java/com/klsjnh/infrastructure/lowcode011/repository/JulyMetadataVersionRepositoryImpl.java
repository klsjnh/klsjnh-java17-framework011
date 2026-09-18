package com.klsjnh.infrastructure.lowcode011.repository;

/*                JulyMetadataVersionRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  july metadata version repository impl class
 *
 */

import com.klsjnh.domain.lowcode011.JulyMetadataVersion;
import com.klsjnh.domain.lowcode011.JulyMetadataVersionRepository;

import com.klsjnh.infrastructure.lowcode011.entity.JulyMetadataVersionPo;
import com.klsjnh.infrastructure.lowcode011.mapper.JulyMetadataVersionMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

/**
 * Repository implementation for publish snapshots. It does NOT extend the base
 * repository: that base treats one business key as one live row, while this
 * table is append-only by (object_name, version).
 */

@Repository
public class JulyMetadataVersionRepositoryImpl implements JulyMetadataVersionRepository {

    /**
     * Snapshot mapper.
     */
    private final JulyMetadataVersionMapper mapper;

    /**
     * Create the repository.
     *
     * @param mapper snapshot mapper
     */
    public JulyMetadataVersionRepositoryImpl(JulyMetadataVersionMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Insert a new snapshot.
     *
     * @param version snapshot
     */
    @Override
    public void insert(JulyMetadataVersion version) {
        JulyMetadataVersionPo po = new JulyMetadataVersionPo();
        po.setId(version.id());
        po.setStatus("1");
        po.setObjectName(version.objectName());
        po.setVersion(version.version());
        po.setPayloadJson(version.payloadJson());
        po.setPhysicalTable(version.physicalTable());
        po.setDdlText(version.ddlText());
        po.setPublishStatus(version.publishStatus());
        po.setPublishedBy(version.publishedBy());
        po.setPublishedAt(version.publishedAt());

        mapper.insert(po);
    }

    /**
     * Find the latest snapshot of an object by publish time.
     *
     * @param objectName object name
     * @return latest snapshot or null
     */
    @Override
    public JulyMetadataVersion findLatest(String objectName) {
        return toDomain(mapper.selectOne(new QueryWrapper<JulyMetadataVersionPo>()
                .eq("object_name", objectName)
                .orderByDesc("published_at", "create_time")
                .last("limit 1")));
    }

    /**
     * Find one snapshot by object name and version.
     *
     * @param objectName object name
     * @param version    version
     * @return snapshot or null
     */
    @Override
    public JulyMetadataVersion findByObjectNameAndVersion(String objectName, String version) {
        return toDomain(mapper.selectOne(new QueryWrapper<JulyMetadataVersionPo>()
                .eq("object_name", objectName)
                .eq("version", version)));
    }

    /**
     * Whether the object name + version pair already exists.
     *
     * @param objectName object name
     * @param version    version
     * @return true when present
     */
    @Override
    public boolean existsByObjectNameAndVersion(String objectName, String version) {
        return mapper.selectCount(new QueryWrapper<JulyMetadataVersionPo>()
                .eq("object_name", objectName)
                .eq("version", version)) > 0;
    }

    /**
     * List all snapshots of an object, newest first.
     *
     * @param objectName object name
     * @return snapshots
     */
    @Override
    public List<JulyMetadataVersion> listByObjectName(String objectName) {
        return mapper.selectList(new QueryWrapper<JulyMetadataVersionPo>()
                .eq("object_name", objectName)
                .orderByDesc("published_at", "create_time"))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Map a PO to the domain record.
     *
     * @param po persistence object, nullable
     * @return domain snapshot or null
     */
    private JulyMetadataVersion toDomain(JulyMetadataVersionPo po) {
        if (po == null) {
            return null;
        }

        return new JulyMetadataVersion(po.getId(), po.getObjectName(), po.getVersion(), po.getPayloadJson(),
                po.getPhysicalTable(), po.getDdlText(), po.getPublishStatus(),
                po.getPublishedBy(), po.getPublishedAt());
    }
}
