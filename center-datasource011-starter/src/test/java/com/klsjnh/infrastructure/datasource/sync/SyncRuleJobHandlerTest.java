package com.klsjnh.infrastructure.datasource.sync;

/*                SyncRuleJobHandlerTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  syncRule handler unit test (payload / missing rule paths)
 *
 */

import com.klsjnh.domain.datasource.sync.JulySyncRule;
import com.klsjnh.domain.datasource.sync.JulySyncRuleRepository;
import com.klsjnh.domain.datasource.sync.SyncEnginePort;
import com.klsjnh.domain.datasource.sync.SyncRunResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link SyncRuleJobHandler}: the payload's syncCode drives the
 * engine, and a missing / blank code or a missing rule never throws (the
 * scheduler contract — a failing handler must not interrupt the schedule).
 */

@ExtendWith(MockitoExtension.class)
class SyncRuleJobHandlerTest {

    /**
     * Mocked sync engine.
     */
    @Mock
    private SyncEnginePort syncEngine;

    /**
     * Mocked rule repository.
     */
    @Mock
    private JulySyncRuleRepository ruleRepository;

    /**
     * Mocked rule.
     */
    @Mock
    private JulySyncRule rule;

    /**
     * Handler under test.
     */
    private SyncRuleJobHandler handler;

    /**
     * Wire the handler with the mocks.
     */
    @BeforeEach
    void setUp() {
        handler = new SyncRuleJobHandler(syncEngine, ruleRepository);
    }

    /**
     * A JSON task_param with a syncCode runs the rule.
     */
    @Test
    void runsRuleFromJsonPayload() {
        when(ruleRepository.findByCode("s1")).thenReturn(rule);
        lenient().when(rule.syncCode()).thenReturn("s1");
        when(syncEngine.run(rule)).thenReturn(new SyncRunResult("s1", 2L, 2L, 1, "ok"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("payload", "{\"syncCode\":\"s1\"}");

        assertDoesNotThrow(() -> handler.execute(payload));
        verify(syncEngine).run(rule);
    }

    /**
     * A missing rule logs a WARN and does not touch the engine.
     */
    @Test
    void missingRuleIsSkipped() {
        when(ruleRepository.findByCode("gone")).thenReturn(null);

        Map<String, Object> payload = new HashMap<>();
        payload.put("payload", "{\"syncCode\":\"gone\"}");

        assertDoesNotThrow(() -> handler.execute(payload));
        verify(syncEngine, org.mockito.Mockito.never()).run(org.mockito.ArgumentMatchers.any());
    }

    /**
     * A blank / missing sync code is skipped without throwing.
     */
    @Test
    void blankCodeIsSkipped() {
        assertDoesNotThrow(() -> handler.execute(new HashMap<>()));
        assertDoesNotThrow(() -> handler.execute(null));

        verify(syncEngine, org.mockito.Mockito.never()).run(org.mockito.ArgumentMatchers.any());
    }

    /**
     * The handler name matches the seeded scheduler_handler value.
     */
    @Test
    void handlerNameIsStable() {
        assertEquals("syncRule", handler.handlerName());
    }
}
