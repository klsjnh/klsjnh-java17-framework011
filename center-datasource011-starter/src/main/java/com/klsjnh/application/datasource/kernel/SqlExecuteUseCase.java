package com.klsjnh.application.datasource.kernel;

/*                SqlExecuteUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  authorized single-statement execute use case (D3)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.SqlGuard011;

import com.klsjnh.domain.datasource.kernel.JulySqlPermissionCodes011;
import com.klsjnh.domain.datasource.kernel.SqlRoutingPort;
import com.klsjnh.domain.iam.auth.AuthorizationPort;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Data source center — execute use case (D3 write access): enforce the
 * {@code datasource:julySql:execute} permission code, rule out multi-statement
 * smuggling, then run the single parameterized statement on a dynamic
 * datasource. The statement runs on a dynamic datasource, so no primary
 * transaction is opened; the params ride PreparedStatement binding, never
 * string concatenation.
 */

@Service
public class SqlExecuteUseCase {

    /**
     * SQL routing port.
     */
    private final SqlRoutingPort sqlRoutingPort;

    /**
     * Authorization port.
     */
    private final AuthorizationPort authorizationPort;

    /**
     * Create the use case.
     *
     * @param sqlRoutingPort    sql routing port
     * @param authorizationPort authorization port
     */
    public SqlExecuteUseCase(SqlRoutingPort sqlRoutingPort, AuthorizationPort authorizationPort) {
        this.sqlRoutingPort = sqlRoutingPort;
        this.authorizationPort = authorizationPort;
    }

    /**
     * Execute one authorized statement on a dynamic datasource.
     *
     * @param operatorId operator id
     * @param dsCode     datasource code
     * @param sql        single statement with {@code ?} placeholders
     * @param params     bound parameters, nullable for none
     * @return affected row count
     */
    public int execute(String operatorId, String dsCode, String sql, List<Object> params) {
        authorizationPort.assertHas(operatorId, JulySqlPermissionCodes011.EXECUTE);
        SqlGuard011.assertExecutable(sql);
        assertScalarParams(params);

        return sqlRoutingPort.execute(dsCode, sql, params);
    }

    /**
     * Reject non-scalar parameters early (a JSON object / array element would
     * fail deep inside the JDBC driver as a 500).
     *
     * @param params bound parameters, nullable
     */
    private void assertScalarParams(List<Object> params) {
        if (params == null) {
            return;
        }

        for (Object param : params) {
            if (param != null && !(param instanceof String) && !(param instanceof Number)
                    && !(param instanceof Boolean)) {
                throw BusinessException.badRequest("sql execute: parameters must be scalars");
            }
        }
    }
}
