package com.klsjnh.infrastructure.datasource.kernel;

/*                DynamicDataSourceRegistryImplTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  testConnection closes the throwaway probe pool (every path)
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.datasource.kernel.ConnectionInfo;

import com.klsjnh.infrastructure.config.KrtDatasourceConfig011;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import java.sql.SQLException;
import java.util.List;

/**
 * Unit tests for {@link DynamicDataSourceRegistryImpl#testConnection}: the
 * throwaway probe pool is closed on every path (success, connect failure,
 * pool build failure).
 */

@ExtendWith(MockitoExtension.class)
class DynamicDataSourceRegistryImplTest {

    /**
     * Mocked pool builder.
     */
    @Mock
    private DataSourcePools011 pools;

    /**
     * Mocked framework config (empty ci011 bootstrap list).
     */
    @Mock
    private KrtDatasourceConfig011 krtConfig;

    /**
     * Mocked probe pool.
     */
    @Mock
    private DruidDataSource probePool;

    /**
     * Registry under test.
     */
    private DynamicDataSourceRegistryImpl registry;

    /**
     * Wire the registry with the mocks.
     */
    @BeforeEach
    void setUp() {
        when(krtConfig.getCi011()).thenReturn(List.of());
        registry = new DynamicDataSourceRegistryImpl(pools, krtConfig);
    }

    /**
     * A successful probe closes the throwaway pool.
     */
    @Test
    void testConnectionClosesProbePoolOnSuccess() throws Exception {
        when(pools.buildForProbe(any(ConnectionInfo.class))).thenReturn(probePool);
        when(probePool.getConnection()).thenReturn(mock(DruidPooledConnection.class));

        assertTrue(registry.testConnection(info()));

        verify(probePool).close();
    }

    /**
     * A failed probe (connect error) still closes the throwaway pool.
     */
    @Test
    void testConnectionClosesProbePoolOnConnectFailure() throws Exception {
        when(pools.buildForProbe(any(ConnectionInfo.class))).thenReturn(probePool);
        when(probePool.getConnection()).thenThrow(new SQLException("connection refused"));

        assertFalse(registry.testConnection(info()));

        verify(probePool).close();
    }

    /**
     * A pool build failure returns false and closes nothing (nothing built).
     */
    @Test
    void testConnectionReturnsFalseWhenPoolBuildFails() {
        when(pools.buildForProbe(any(ConnectionInfo.class)))
                .thenThrow(BusinessException.badRequest("unknown database type"));

        assertFalse(registry.testConnection(info()));

        verify(probePool, never()).close();
    }

    /**
     * Sample connection info.
     *
     * @return connection info
     */
    private ConnectionInfo info() {
        return new ConnectionInfo("dsA", "Data source A", "mysql", "jdbc:mysql://127.0.0.1:3306/db", "u", "p", null,
                null, null);
    }
}
