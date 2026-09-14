package com.klsjnh.demo11.infrastructure.persistence.mapper;

/*                Demo011Mapper interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  demo 011 mapper interface
 *
 */

import com.klsjnh.demo11.infrastructure.persistence.entity.Demo011Po;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for the july_demo011 table.
 */

@Mapper
public interface Demo011Mapper extends BaseMapper<Demo011Po> {
}
