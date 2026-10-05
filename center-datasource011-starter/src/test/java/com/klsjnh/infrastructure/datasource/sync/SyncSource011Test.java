package com.klsjnh.infrastructure.datasource.sync;

/*                SyncSource011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  source guard unit test (identifier whitelist + read-only sql)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.datasource.kernel.SqlRoutingPort;
import com.klsjnh.domain.datasource.sync.Endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for the sync source guard: table endpoints are whitelisted
 * identifiers, sql endpoints must pass the read-only guard, and a rejected
 * endpoint never reaches the routing port.
 */

@ExtendWith(MockitoExtension.class)
class SyncSource011Test {

    /**
     * Mocked routing port (never touched by a rejected endpoint).
     */
    @Mock
    private SqlRoutingPort sqlRoutingPort;

    /**
     * Source under test.
     */
    private SyncSource011 source;

    /**
     * Wire the source with the mocked port.
     */
    @BeforeEach
    void setUp() {
        source = new SyncSource011(sqlRoutingPort);
    }

    /**
     * A table endpoint becomes a plain SELECT of the whitelisted name.
     */
    @Test
    void tableEndpointBuildsSelectFromIdentifier() {
        when(sqlRoutingPort.selectListByPage("dsA", "SELECT * FROM july_user", null, 1, 500))
                .thenReturn(new PageResult011<>(1, 500, 1, 1, List.of(Map.of("id", "1"))));

        List<Map<String, Object>> rows = source.readPage(new Endpoint("dsA", "table", "july_user"), 500, 1);

        assertEquals(1, rows.size());
    }

    /**
     * A table endpoint with a non-identifier name (statement tail, quote,
     * blank) is rejected before the routing port is reached.
     */
    @Test
    void tableEndpointRejectsIllegalIdentifier() {
        assertThrows(BusinessException.class,
                () -> source.readPage(new Endpoint("dsA", "table", "july_user; DROP TABLE x"), 500, 1));

        assertThrows(BusinessException.class,
                () -> source.readPage(new Endpoint("dsA", "table", "july_user`"), 500, 1));

        assertThrows(BusinessException.class, () -> source.readPage(new Endpoint("dsA", "table", null), 500, 1));

        verify(sqlRoutingPort, never()).selectListByPage(anyString(), anyString(), any(), anyInt(), anyInt());
    }

    /**
     * A sql endpoint passes the read-only guard and executes as authored.
     */
    @Test
    void sqlEndpointPassesReadOnlySelect() {
        when(sqlRoutingPort.selectListByPage("dsA", "SELECT id FROM july_user", null, 1, 500))
                .thenReturn(new PageResult011<>(1, 500, 0, 0, List.of()));

        List<Map<String, Object>> rows = source.readPage(new Endpoint("dsA", "sql", "SELECT id FROM july_user"), 500,
                1);

        assertEquals(0, rows.size());
    }

    /**
     * A sql endpoint carrying DML is rejected by the read-only guard.
     */
    @Test
    void sqlEndpointRejectsDml() {
        assertThrows(BusinessException.class,
                () -> source.readPage(new Endpoint("dsA", "sql", "DELETE FROM july_user"), 500, 1));

        assertThrows(BusinessException.class,
                () -> source.readPage(new Endpoint("dsA", "sql", "SELECT 1; DELETE FROM july_user"), 500, 1));

        verify(sqlRoutingPort, never()).selectListByPage(anyString(), anyString(), any(), anyInt(), anyInt());
    }
}
