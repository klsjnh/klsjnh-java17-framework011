package com.klsjnh.infrastructure.lowcode011.mapper;

/*                JulyMetadataFieldMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata field mapper interface
 *
 */

import com.klsjnh.infrastructure.lowcode011.entity.JulyMetadataFieldPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis-Plus mapper for the july_metadata_field table.
 */

@Mapper
public interface JulyMetadataFieldMapper extends BaseMapper<JulyMetadataFieldPo> {

    /**
     * Physically delete all children of a master (replace strategy — child rows
     * are owned and have no tombstone value, so the composite unique key is
     * freed for the re-insert).
     *
     * @param masterId master id
     * @return deleted row count
     */
    @Delete("DELETE FROM july_metadata_field WHERE pk_mt = #{masterId}")
    int deleteByMaster(@Param("masterId") String masterId);
}
