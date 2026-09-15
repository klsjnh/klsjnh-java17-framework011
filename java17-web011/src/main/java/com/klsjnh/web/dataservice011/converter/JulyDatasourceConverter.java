package com.klsjnh.web.dataservice011.converter;

/*                JulyDatasourceConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource converter class
 *
 */

import com.klsjnh.domain.dataservice011.JulyDatasource;
import com.klsjnh.domain.datasource.DataSourceProbePort;

import com.klsjnh.web.dataservice011.vo.julydatasource.JulyDatasourceTestResultVo011;
import com.klsjnh.web.dataservice011.vo.julydatasource.JulyDatasourceVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyDatasource aggregate and the response VO.
 * <p>
 * This class is the write-only boundary of the password: the target VO simply
 * has no such field, so there is no code path here that could leak it.
 * </p>
 */

@Component
public class JulyDatasourceConverter {

    /**
     * Map the aggregate to the response VO.
     *
     * @param datasource aggregate
     * @return response VO (without any password field)
     */
    public JulyDatasourceVo011 toVo(JulyDatasource datasource) {
        JulyDatasourceVo011 vo = new JulyDatasourceVo011();
        vo.setId(datasource.id().value());
        vo.setDsCode(datasource.dsCode());
        vo.setDsName(datasource.dsName());
        vo.setDbType(datasource.dbType());
        vo.setJdbcUrl(datasource.jdbcUrl());
        vo.setSchemaName(datasource.schemaName());
        vo.setUsername(datasource.username());
        vo.setDriverClass(datasource.driverClass());
        vo.setPoolConfig(datasource.poolConfig());
        vo.setRemark(datasource.remark());
        vo.setStatus(datasource.status());
        vo.setCreateBy(datasource.audit().createBy());
        vo.setUpdateBy(datasource.audit().updateBy());
        vo.setCreateTime(datasource.audit().createTime());
        vo.setUpdateTime(datasource.audit().updateTime());

        return vo;
    }

    /**
     * Map aggregates to response VOs.
     *
     * @param datasources aggregates
     * @return response VO list
     */
    public List<JulyDatasourceVo011> toVoList(List<JulyDatasource> datasources) {
        List<JulyDatasourceVo011> result = new ArrayList<>();

        for (JulyDatasource datasource : datasources) {
            result.add(toVo(datasource));
        }

        return result;
    }

    /**
     * Map a probe result to its response VO.
     *
     * @param result probe result
     * @return test result VO
     */
    public JulyDatasourceTestResultVo011 toTestResultVo(DataSourceProbePort.ProbeResult result) {
        JulyDatasourceTestResultVo011 vo = new JulyDatasourceTestResultVo011();
        vo.setSuccess(result.success());
        vo.setMessage(result.message());
        vo.setDatabaseProduct(result.databaseProduct());
        vo.setDatabaseVersion(result.databaseVersion());

        return vo;
    }
}
