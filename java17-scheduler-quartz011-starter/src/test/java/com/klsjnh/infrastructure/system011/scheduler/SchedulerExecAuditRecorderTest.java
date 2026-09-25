package com.klsjnh.infrastructure.system011.scheduler;

/*                SchedulerExecAuditRecorderTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  unit test for scheduler exec audit recorder
 *
 */

import com.klsjnh.domain.system011.scheduler.JulySchedulerAuditRepository;
import com.klsjnh.domain.system011.scheduler.SchedulerExecStatus011;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

/**
 * Unit tests for {@link SchedulerExecAuditRecorder}: truncate + insert path.
 */

@ExtendWith(MockitoExtension.class)
class SchedulerExecAuditRecorderTest {

    @Mock
    private JulySchedulerAuditRepository auditRepository;

    @InjectMocks
    private SchedulerExecAuditRecorder recorder;

    /**
     * Truncate leaves short messages unchanged and nulls alone.
     */
    @Test
    void truncateKeepsShortAndNull() {
        assertNull(SchedulerExecAuditRecorder.truncate(null));
        assertEquals("ok", SchedulerExecAuditRecorder.truncate("ok"));
    }

    /**
     * Truncate caps at ERROR_MESSAGE_MAX characters.
     */
    @Test
    void truncateCapsAtMax() {
        String longMsg = "x".repeat(SchedulerExecAuditRecorder.ERROR_MESSAGE_MAX + 40);
        String truncated = SchedulerExecAuditRecorder.truncate(longMsg);

        assertEquals(SchedulerExecAuditRecorder.ERROR_MESSAGE_MAX, truncated.length());
    }

    /**
     * Record inserts SUCCESS with truncated error null.
     */
    @Test
    void recordInsertsSuccess() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 26, 1, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 26, 1, 1);

        recorder.record("sch-1", "demoJob", start, end, SchedulerExecStatus011.SUCCESS, null);

        verify(auditRepository).insert(eq("sch-1"), eq("demoJob"), eq(start), eq(end), eq("SUCCESS"), isNull());
    }

    /**
     * Record truncates FAIL error_message before insert.
     */
    @Test
    void recordTruncatesFailMessage() {
        String longMsg = "e".repeat(SchedulerExecAuditRecorder.ERROR_MESSAGE_MAX + 10);
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusSeconds(1);

        recorder.record("sch-2", "demoJob", start, end, SchedulerExecStatus011.FAIL, longMsg);

        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditRepository).insert(eq("sch-2"), eq("demoJob"), eq(start), eq(end), eq("FAIL"),
                msgCaptor.capture());
        assertEquals(SchedulerExecAuditRecorder.ERROR_MESSAGE_MAX, msgCaptor.getValue().length());
    }

    /**
     * Insert failures are swallowed (never rethrown).
     */
    @Test
    void recordSwallowsRepositoryFailure() {
        doThrow(new RuntimeException("db down")).when(auditRepository).insert(anyString(), any(), any(), any(),
                anyString(), any());

        recorder.record("sch-3", null, LocalDateTime.now(), LocalDateTime.now(), SchedulerExecStatus011.FAIL,
                "boom");
    }
}
