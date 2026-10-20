package com.klsjnh.infrastructure.datasource.sync;

/*                SyncEngine011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.11
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.11  page size clamp test — an oversized rule pageSize must not
 *                  stop the run after the first page
 *
 */

import com.klsjnh.domain.datasource.sync.Endpoint;
import com.klsjnh.domain.datasource.sync.JulySyncRule;
import com.klsjnh.domain.datasource.sync.JulySyncRuleColumn;
import com.klsjnh.domain.datasource.sync.JulySyncRuleColumnRepository;
import com.klsjnh.domain.datasource.sync.SyncRunResult;
import com.klsjnh.domain.datasource.sync.SyncSinkPort;
import com.klsjnh.domain.datasource.sync.SyncSourcePort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link SyncEngine011}: the page size the engine drives must
 * stay inside the routing port's [10, 500] clamp, so a rule configured with an
 * oversized pageSize still walks every page instead of stopping after the
 * first one.
 */

class SyncEngine011Test {

    /**
     * Source fake that mimics the routing port's [10, 500] clamp (it returns
     * at most 500 rows per page, whatever was requested) and records the page
     * sizes it was asked for.
     */
    private static final class ClampedSource implements SyncSourcePort {

        /**
         * Page sizes requested so far.
         */
        final List<Integer> requestedSizes = new ArrayList<>();

        @Override
        public List<Map<String, Object>> readPage(Endpoint endpoint, int pageSize, int pageIndex) {
            requestedSizes.add(pageSize);

            if (pageIndex > 2) {
                return List.of();
            }

            List<Map<String, Object>> rows = new ArrayList<>();

            for (int i = 0; i < Math.min(pageSize, 500); i++) {
                rows.add(Map.of("id", pageIndex * 1000 + i));
            }

            return rows;
        }
    }

    /**
     * Sink fake that counts the rows it received.
     */
    private static final class CountingSink implements SyncSinkPort {

        /**
         * Rows written so far.
         */
        long written = 0;

        @Override
        public int writePage(Endpoint endpoint, String targetTable, List<String> keyColumns, boolean upsert,
                List<Map<String, Object>> rows) {
            written += rows.size();

            return rows.size();
        }
    }

    /**
     * Engine with one id mapping, the clamped source fake and the counting
     * sink fake.
     *
     * @param source source fake
     * @param sink   sink fake
     * @return engine under test
     */
    private SyncEngine011 engine(SyncSourcePort source, SyncSinkPort sink) {
        return new SyncEngine011(new JulySyncRuleColumnRepository() {
            /** {@inheritDoc} */
            @Override
            public void insert(JulySyncRuleColumn column) {
            }

            /** {@inheritDoc} */
            @Override
            public void update(JulySyncRuleColumn column) {
            }

            /** {@inheritDoc} */
            @Override
            public JulySyncRuleColumn findById(String id) {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            public List<JulySyncRuleColumn> findByMaster(String pkMt) {
                return List.of(JulySyncRuleColumn.create(EntityId.generate(), "pk-test", "id", "long", "id", "long",
                        null, null, null, AuditInfo.empty()));
            }

            /** {@inheritDoc} */
            @Override
            public long countByMaster(String pkMt, boolean enabledOnly) {
                return 1;
            }

            /** {@inheritDoc} */
            @Override
            public boolean logicDeleteById(String id) {
                return true;
            }

            /** {@inheritDoc} */
            @Override
            public void logicDeleteByMaster(String pkMt) {
            }
        }, source, sink);
    }

    /**
     * A rule with the given pageSize (nullable).
     *
     * @param pageSize configured page size
     * @return sync rule
     */
    private JulySyncRule rule(Integer pageSize) {
        return JulySyncRule.create(EntityId.generate(), "s1", "rule one", "ds-src", Endpoint.KIND_SQL,
                "SELECT id FROM src", "ds-tgt", Endpoint.KIND_TABLE, "t_target", "full", "id", "upsert", pageSize,
                null, AuditInfo.empty());
    }

    /**
     * Regression: a rule configured with pageSize 5000 used to stop after the
     * first page (the routing port clamps pages to 500 rows, 500 &lt; 5000);
     * the engine now drives the clamped size and walks all pages.
     */
    @Test
    void oversizedPageSizeWalksAllPages() {
        ClampedSource source = new ClampedSource();
        CountingSink sink = new CountingSink();

        SyncRunResult result = engine(source, sink).run(rule(5000));

        assertEquals(500, source.requestedSizes.get(0));
        assertEquals(2, result.pages());
        assertEquals(1000, result.read());
        assertEquals(1000, result.written());
    }

    /**
     * A rule without a configured page size drives the default 500.
     */
    @Test
    void defaultPageSizeIs500() {
        ClampedSource source = new ClampedSource();

        engine(source, new CountingSink()).run(rule(null));

        assertEquals(500, source.requestedSizes.get(0));
    }
}
