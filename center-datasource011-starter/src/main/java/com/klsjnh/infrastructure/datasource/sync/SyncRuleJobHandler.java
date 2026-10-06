package com.klsjnh.infrastructure.datasource.sync;

/*                SyncRuleJobHandler class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  syncRule job handler (scheduler -> sync engine, D5)
 *
 */

import lombok.extern.slf4j.Slf4j;

import com.klsjnh.domain.datasource.sync.JulySyncRule;
import com.klsjnh.domain.datasource.sync.JulySyncRuleRepository;
import com.klsjnh.domain.datasource.sync.SyncEnginePort;
import com.klsjnh.domain.datasource.sync.SyncRunResult;
import com.klsjnh.domain.system011.scheduler.JobHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Scheduler handler for sync rules ({@code scheduler_handler = "syncRule"}):
 * the task payload carries {@code {"syncCode":"..."}} and the tick runs the
 * rule once through the sync engine.
 * <p>
 * System identity: a scheduled tick is initiated by the operator who STARTED
 * the schedule (which required {@code system011:julyScheduler:start}), so the
 * tick itself does not go through {@code datasource:julySyncRule:run}. A
 * missing / blank sync code or a missing rule logs a WARN and never interrupts
 * the schedule (same contract as the scheduler job wrapper).
 * </p>
 */

@Slf4j
public class SyncRuleJobHandler implements JobHandler {

    /**
     * Handler name stored in july_scheduler.scheduler_handler.
     */
    public static final String HANDLER_NAME = "syncRule";

    /**
     * Payload key holding the sync rule code.
     */
    public static final String KEY_SYNC_CODE = "syncCode";

    /**
     * JSON mapper for the task payload (static: thread-safe).
     */
    private static final ObjectMapper PAYLOAD_MAPPER = new ObjectMapper();

    /**
     * Sync engine.
     */
    private final SyncEnginePort syncEngine;

    /**
     * Sync rule repository.
     */
    private final JulySyncRuleRepository ruleRepository;

    /**
     * Create the handler.
     *
     * @param syncEngine     sync engine
     * @param ruleRepository sync rule repository
     */
    public SyncRuleJobHandler(SyncEnginePort syncEngine, JulySyncRuleRepository ruleRepository) {
        this.syncEngine = syncEngine;
        this.ruleRepository = ruleRepository;
    }

    /**
     * Get the unique handler name used in july_scheduler.scheduler_handler.
     *
     * @return handler name
     */
    @Override
    public String handlerName() {
        return HANDLER_NAME;
    }

    /**
     * Execute the task: run one sync rule by code.
     *
     * @param payload job payload from the scheduler, may be empty
     */
    @Override
    public void execute(Map<String, Object> payload) {
        String funcName = "sync rule tick";

        String syncCode = extractSyncCode(payload);

        if (syncCode == null || syncCode.isBlank()) {
            log.warn("{} payload missing {} ...", funcName, KEY_SYNC_CODE);
            return;
        }

        JulySyncRule rule = ruleRepository.findByCode(syncCode);

        if (rule == null) {
            log.warn("{} rule not found {} ...", funcName, syncCode);
            return;
        }

        SyncRunResult result = syncEngine.run(rule);

        log.info("{} {} read {} written {} pages {} ...", funcName, syncCode, result.read(), result.written(),
                result.pages());
    }

    /**
     * Extract the sync code: the task_param JSON travels in the payload under
     * {@code payload}; a flat {@code syncCode} key is also accepted.
     *
     * @param payload job payload
     * @return sync code, nullable
     */
    @SuppressWarnings("unchecked")
    private String extractSyncCode(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }

        Object flat = payload.get(KEY_SYNC_CODE);

        if (flat instanceof String value && !value.isBlank()) {
            return value.trim();
        }

        Object json = payload.get("payload");

        if (json instanceof String text && !text.isBlank()) {
            try {
                Map<String, Object> parsed = PAYLOAD_MAPPER.readValue(text, Map.class);

                Object code = parsed.get(KEY_SYNC_CODE);

                return code == null ? null : String.valueOf(code).trim();
            } catch (Exception ex) {
                log.warn("sync rule tick payload is not valid JSON {} ...", ex.getMessage());
                return null;
            }
        }

        return null;
    }
}
