package com.klsjnh.web.storagecenter.converter;

/*                JulyStorageConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage converter class
 *
 */

import com.klsjnh.domain.storagecenter.JulyStorage;

import com.klsjnh.web.storagecenter.vo.JulyStorageVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyStorage aggregate and the response VO (access /
 * secret keys masked, {@code secure} as the legacy "1" / "0" code).
 */

@Component
public class JulyStorageConverter {

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
    public JulyStorageVo011 toVo(JulyStorage storage) {
        JulyStorageVo011 vo = new JulyStorageVo011();
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
        vo.setDefaultBucket(storage.defaultBucket());
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
    public List<JulyStorageVo011> toVoList(List<JulyStorage> storages) {
        List<JulyStorageVo011> result = new ArrayList<>();

        for (JulyStorage storage : storages) {
            result.add(toVo(storage));
        }

        return result;
    }
}
