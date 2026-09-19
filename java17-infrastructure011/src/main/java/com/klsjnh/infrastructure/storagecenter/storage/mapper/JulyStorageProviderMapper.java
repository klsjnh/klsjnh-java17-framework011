package com.klsjnh.infrastructure.storagecenter.storage.mapper;

/*                JulyStorageProviderMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage provider mapper interface
 *
 */

import com.klsjnh.infrastructure.storagecenter.storage.entity.JulyStorageProviderPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_storage_provider table.
 */

@Mapper
public interface JulyStorageProviderMapper extends BaseMapper<JulyStorageProviderPo> {
}
