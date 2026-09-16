package com.klsjnh.web.storage011.converter;

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

import com.klsjnh.domain.storage.JulyStorage;

import com.klsjnh.web.storage011.vo.JulyStorageVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyStorage aggregate and the response VO (no
 * secretKey).
 */

@Component
public class JulyStorageConverter {

    /**
     * Map the aggregate to the response VO (without secretKey).
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
        vo.setAccessKey(storage.accessKey());
        vo.setSecure(storage.secure());
        vo.setDefaultBucket(storage.defaultBucket());
        vo.setPresignExpirySeconds(storage.presignExpirySeconds());
        vo.setRemark(storage.remark());
        vo.setStatus(storage.status());
        vo.setCreateBy(storage.audit().createBy());
        vo.setUpdateBy(storage.audit().updateBy());
        vo.setCreateTime(storage.audit().createTime());
        vo.setUpdateTime(storage.audit().updateTime());

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
