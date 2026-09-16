package com.klsjnh.infrastructure.lowcode011.mapper;

/*                JulyMetadataServiceMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata service mapper interface
 *
 */

import com.klsjnh.infrastructure.lowcode011.entity.JulyMetadataServicePo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis-Plus mapper for the july_metadata_service table.
 */

@Mapper
public interface JulyMetadataServiceMapper extends BaseMapper<JulyMetadataServicePo> {

    /**
     * Physically delete all children of a master (replace strategy).
     *
     * @param masterId master id
     * @return deleted row count
     */
    @Delete("DELETE FROM july_metadata_service WHERE pk_mt = #{masterId}")
    int deleteByMaster(@Param("masterId") String masterId);
}
