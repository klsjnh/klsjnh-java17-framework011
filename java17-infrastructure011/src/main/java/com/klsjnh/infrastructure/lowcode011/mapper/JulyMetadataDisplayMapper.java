package com.klsjnh.infrastructure.lowcode011.mapper;

/*                JulyMetadataDisplayMapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata display mapper interface
 *
 */

import com.klsjnh.infrastructure.lowcode011.entity.JulyMetadataDisplayPo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis-Plus mapper for the july_metadata_display table.
 */

@Mapper
public interface JulyMetadataDisplayMapper extends BaseMapper<JulyMetadataDisplayPo> {

    /**
     * Physically delete all children of a master (replace strategy).
     *
     * @param masterId master id
     * @return deleted row count
     */
    @Delete("DELETE FROM july_metadata_display WHERE pk_mt = #{masterId}")
    int deleteByMaster(@Param("masterId") String masterId);
}
