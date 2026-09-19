package com.klsjnh.infrastructure.storagecenter.storage.mapper;

/*                JulyStorageProviderBucketMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july storage provider bucket mapper interface
 *
 */

import com.klsjnh.infrastructure.storagecenter.storage.entity.JulyStorageProviderBucketPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_storage_provider_bucket table.
 */

@Mapper
public interface JulyStorageProviderBucketMapper extends BaseMapper<JulyStorageProviderBucketPo> {
}
