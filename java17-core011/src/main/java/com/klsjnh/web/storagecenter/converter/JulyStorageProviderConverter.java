package com.klsjnh.web.storagecenter.converter;

/*                JulyStorageProviderConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage provider converter class
 *
 */

import com.klsjnh.domain.storagecenter.storage.JulyStorageProvider;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucket;

import com.klsjnh.web.storagecenter.vo.bucket.StorageBucketVo011;
import com.klsjnh.web.storagecenter.vo.storage.JulyStorageProviderVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyStorageProvider / JulyStorageProviderBucket
 * aggregates and the response VOs (access / secret keys masked,
 * {@code secure} as the legacy "1" / "0" code).
 */

@Component
public class JulyStorageProviderConverter {

    /**
     * Mask echoed back for the access key / secret key.
     */
    private static final String MASK_SECRET = "******";

    /**
     * Map the aggregate to the response VO (keys masked).
     *
     * @param storage aggregate
     * @return response VO
     */
    public JulyStorageProviderVo011 toVo(JulyStorageProvider storage) {
        JulyStorageProviderVo011 vo = new JulyStorageProviderVo011();
        vo.setId(storage.id().value());
        vo.setStorageCode(storage.storageCode());
        vo.setSortOrder(storage.sortOrder());
        vo.setStorageName(storage.storageName());
        vo.setProvider(storage.provider());
        vo.setBasePath(storage.basePath());
        vo.setEndpoint(storage.endpoint());
        vo.setAccessKey(MASK_SECRET);
        vo.setSecretKey(MASK_SECRET);
        vo.setSecure(storage.secure() ? "1" : "0");
        vo.setPresignExpirySeconds(storage.presignExpirySeconds());
        vo.setRemark(storage.remark());
        vo.setStatus(storage.status());
        vo.setCreatedBy(storage.audit().createBy());
        vo.setUpdatedBy(storage.audit().updateBy());
        vo.setCreateDate(storage.audit().createTime());
        vo.setModifyDate(storage.audit().updateTime());

        return vo;
    }

    /**
     * Map aggregates to response VOs.
     *
     * @param storages aggregates
     * @return response VO list
     */
    public List<JulyStorageProviderVo011> toVoList(List<JulyStorageProvider> storages) {
        List<JulyStorageProviderVo011> result = new ArrayList<>();

        for (JulyStorageProvider storage : storages) {
            result.add(toVo(storage));
        }

        return result;
    }

    /**
     * Map a bucket entity to its response VO.
     *
     * @param bucket entity
     * @return response VO
     */
    public StorageBucketVo011 toBucketVo(JulyStorageProviderBucket bucket) {
        StorageBucketVo011 vo = new StorageBucketVo011();
        vo.setBucketCode(bucket.bucketCode());
        vo.setBucketName(bucket.bucketName());
        vo.setIsDefault(bucket.isDefault());
        vo.setSortOrder(bucket.sortOrder());
        vo.setStatus(bucket.status());
        vo.setRemark(bucket.remark());
        vo.setCreatedBy(bucket.audit().createBy());
        vo.setUpdatedBy(bucket.audit().updateBy());
        vo.setCreateDate(bucket.audit().createTime());
        vo.setModifyDate(bucket.audit().updateTime());

        return vo;
    }

    /**
     * Map bucket entities to response VOs.
     *
     * @param buckets entities
     * @return response VO list
     */
    public List<StorageBucketVo011> toBucketVoList(List<JulyStorageProviderBucket> buckets) {
        List<StorageBucketVo011> result = new ArrayList<>();

        for (JulyStorageProviderBucket bucket : buckets) {
            result.add(toBucketVo(bucket));
        }

        return result;
    }
}
