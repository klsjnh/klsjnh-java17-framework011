package com.klsjnh.application.datasource.kernel;

/*                SqlExecuteUseCaseTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  execute use case unit test (permission + single statement)
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.datasource.kernel.JulySqlPermissionCodes011;
import com.klsjnh.domain.datasource.kernel.SqlRoutingPort;
import com.klsjnh.domain.iam.auth.AuthorizationPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

/**
 * Unit tests for {@link SqlExecuteUseCase}: the EXECUTE permission is asserted,
 * a multi-statement payload is rejected before routing, and a clean statement
 * rides the parameterized routing port.
 */

@ExtendWith(MockitoExtension.class)
class SqlExecuteUseCaseTest {

    /**
     * Mocked routing port.
     */
    @Mock
    private SqlRoutingPort sqlRoutingPort;

    /**
     * Mocked authorization port.
     */
    @Mock
    private AuthorizationPort authorizationPort;

    /**
     * Use case under test.
     */
    private SqlExecuteUseCase useCase;

    /**
     * Wire the use case with the mocks.
     */
    @BeforeEach
    void setUp() {
        useCase = new SqlExecuteUseCase(sqlRoutingPort, authorizationPort);
    }

    /**
     * A clean statement asserts the EXECUTE code and executes with params.
     */
    @Test
    void executeAssertsPermissionAndRoutes() {
        when(sqlRoutingPort.execute("dsA", "UPDATE t SET name = ? WHERE id = ?", List.of("n1"))).thenReturn(1);

        int affected = useCase.execute("op-1", "dsA", "UPDATE t SET name = ? WHERE id = ?", List.of("n1"));

        assertEquals(1, affected);
        verify(authorizationPort).assertHas("op-1", JulySqlPermissionCodes011.EXECUTE);
    }

    /**
     * A multi-statement payload is rejected before the routing port is
     * reached.
     */
    @Test
    void multiStatementIsRejected() {
        assertThrows(BusinessException.class,
                () -> useCase.execute("op-1", "dsA", "UPDATE t SET a = 1; DROP TABLE t", null));

        verify(sqlRoutingPort, never()).execute(anyString(), anyString(), any());
        verify(authorizationPort).assertHas(eq("op-1"), anyString());
    }

    /**
     * A blank statement is rejected.
     */
    @Test
    void blankStatementIsRejected() {
        assertThrows(BusinessException.class, () -> useCase.execute("op-1", "dsA", " ", null));

        verify(sqlRoutingPort, never()).execute(anyString(), anyString(), any());
    }
}
