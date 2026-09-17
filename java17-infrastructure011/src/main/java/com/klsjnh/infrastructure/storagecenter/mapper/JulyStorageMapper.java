package com.klsjnh.infrastructure.storagecenter.mapper;

/*                JulyStorageMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage mapper interface
 *
 */

import com.klsjnh.infrastructure.storagecenter.entity.JulyStoragePo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_storage table.
 */

@Mapper
public interface JulyStorageMapper extends BaseMapper<JulyStoragePo> {
}
