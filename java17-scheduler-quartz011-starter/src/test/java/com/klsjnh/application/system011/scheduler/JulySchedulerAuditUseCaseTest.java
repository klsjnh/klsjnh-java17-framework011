package com.klsjnh.application.system011.scheduler;

/*                JulySchedulerAuditUseCaseTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  unit test for july scheduler audit use case
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.system011.scheduler.JulySchedulerAuditRepository;
import com.klsjnh.domain.system011.scheduler.JulySchedulerAuditRow;
import com.klsjnh.domain.system011.scheduler.JulySchedulerPermissionCodes011;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Unit tests for {@link JulySchedulerAuditUseCase}: permission + page by
 * pkMt.
 */

@ExtendWith(MockitoExtension.class)
class JulySchedulerAuditUseCaseTest {

    @Mock
    private JulySchedulerAuditRepository auditRepository;

    @Mock
    private AuthorizationPort authorizationPort;

    private JulySchedulerAuditUseCase useCase;

    /**
     * Wire the use case before each test.
     */
    @BeforeEach
    void setUp() {
        useCase = new JulySchedulerAuditUseCase(auditRepository, authorizationPort);
    }

    /**
     * Blank pkMt is rejected before the repository is hit.
     */
    @Test
    void selectListByPageRequiresPkMt() {
        assertThrows(BusinessException.class,
                () -> useCase.selectListByPage("op-1", new PageQuery011(1, 10), "  "));

        verify(authorizationPort).assertHas("op-1", JulySchedulerPermissionCodes011.SELECT);
    }

    /**
     * Happy path returns repository page under SELECT permission.
     */
    @Test
    void selectListByPageReturnsRows() {
        JulySchedulerAuditRow row = new JulySchedulerAuditRow("a1", "sch-1", "demo", LocalDateTime.now(),
                LocalDateTime.now(), "SUCCESS", null, LocalDateTime.now());
        when(auditRepository.findPageByScheduler(0, 10, "sch-1")).thenReturn(List.of(row));
        when(auditRepository.countByScheduler("sch-1")).thenReturn(1L);

        PageResult011<JulySchedulerAuditRow> page = useCase.selectListByPage("op-1", new PageQuery011(1, 10),
                "sch-1");

        assertEquals(1, page.rows().size());
        assertEquals("a1", page.rows().get(0).id());
        assertEquals(1L, page.total());
        verify(authorizationPort).assertHas("op-1", JulySchedulerPermissionCodes011.SELECT);
    }
}
