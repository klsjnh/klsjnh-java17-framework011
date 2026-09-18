package com.klsjnh.infrastructure.datasource.seed;

/*                DatasourceSeed011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  datasource seed 011 class
 *
 */

import lombok.extern.slf4j.Slf4j;

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.datasource.DatasourceSeedPort;
import com.klsjnh.domain.datasource.JulyDatasource;
import com.klsjnh.domain.datasource.JulyDatasourceRepository;
import com.klsjnh.domain.datasource.SeedResult011;
import com.klsjnh.domain.datasource.ConnectionInfo;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrap seed implementation: turns the yaml list into table rows, one
 * dsCode at a time.
 * <p>
 * The yaml is a <b>one-shot seed</b>, never a second source of truth: an entry
 * only ever creates a row, it never updates or revives one. After the first
 * successful boot the table is the sole runtime authority, so editing the yaml
 * of an existing dsCode has no effect — that is intentional, and the mismatch
 * is surfaced as a WARN so the operator is not left guessing.
 * </p>
 */

@Slf4j
@Component
public class DatasourceSeed011 implements DatasourceSeedPort {

    /**
     * JulyDatasource aggregate repository.
     */
    private final JulyDatasourceRepository repository;

    /**
     * Create the seed.
     *
     * @param repository july datasource repository
     */
    public DatasourceSeed011(JulyDatasourceRepository repository) {
        this.repository = repository;
    }

    /**
     * Seed the table from the bootstrap list (see the port contract).
     *
     * @param fromYaml bootstrap configs, null or empty means nothing to do
     * @return seed summary, never null
     */
    @Override
    public SeedResult011 seed(List<ConnectionInfo> fromYaml) {
        if (fromYaml == null || fromYaml.isEmpty()) {
            log.info("yaml seed skipped: no krt.ci011 entry ...");
            return SeedResult011.empty();
        }

        int inserted = 0;
        int skipped = 0;
        int failed = 0;

        for (ConnectionInfo info : fromYaml) {
            if (info == null || StringUtil011.isBlank(info.dsCode())) {
                failed++;
                log.error("yaml seed entry without dsCode, skipped ...");
                continue;
            }

            String dsCode = info.dsCode();

            try {
                if (repository.existsIncludingDeleted(dsCode)) {
                    skipped++;
                    warnOnDrift(dsCode, info);
                    continue;
                }

                repository.insert(newAggregate(dsCode, info));
                inserted++;
                log.info("yaml seed inserted {} ...", dsCode);
            } catch (Exception ex) {
                failed++;
                log.error("yaml seed failed for datasource code {} — startup continues, repair the entry and restart, or create it from the management page", dsCode, ex);
            }
        }

        SeedResult011 result = SeedResult011.of(fromYaml.size(), inserted, skipped, failed);

        log.info("yaml seed total {} inserted {} skipped {} failed {} ...",
                result.total(), result.inserted(), result.skipped(), result.failed());

        return result;
    }

    /**
     * Compare a yaml entry against the stored row and WARN on drift.
     * <p>
     * The stored row wins and is never overwritten; the WARN tells the operator
     * which fields disagree so they can decide whether the yaml is stale or the
     * row was edited on purpose. A logic-deleted row has no comparable content,
     * so it is reported as a tombstone instead.
     * </p>
     *
     * @param dsCode datasource code
     * @param info   the yaml entry
     */
    private void warnOnDrift(String dsCode, ConnectionInfo info) {
        JulyDatasource stored = repository.findByCode(dsCode);

        if (stored == null) {
            log.warn("yaml seed skipped {} — stored row is logic deleted (tombstone), the seed will never revive it",
                    dsCode);
            return;
        }

        List<String> drifted = new ArrayList<>();

        if (!StringUtil011.equalsTrimmed(stored.dsName(), info.dsName())) {
            drifted.add("dsName");
        }

        if (!StringUtil011.equalsTrimmed(stored.jdbcUrl(), info.dsUrl())) {
            drifted.add("jdbcUrl");
        }

        if (!StringUtil011.equalsTrimmed(stored.dbType(), info.dsType())) {
            drifted.add("dbType");
        }

        if (!drifted.isEmpty()) {
            log.warn("yaml seed skipped {} — stored row differs on {}; the table wins, update the table from the management page if the yaml is the intended value", dsCode, String.join(", ", drifted));
        }
    }

    /**
     * Build the aggregate for a yaml entry.
     * <p>
     * The yaml shape carries no driverClass override and no pool config, so both
     * stay null and the pool builder falls back to the database type default.
     * A domain validation failure surfaces as an IllegalArgumentException, which
     * the caller logs at ERROR and skips.
     * </p>
     *
     * @param dsCode datasource code
     * @param info   the yaml entry
     * @return new aggregate
     */
    private JulyDatasource newAggregate(String dsCode, ConnectionInfo info) {
        return JulyDatasource.create(EntityId.generate(), dsCode, null, info.dsName(), info.dsType(), info.dsUrl(),
                info.schemaName(), info.username(), info.password(), info.driverClass(), null, AuditInfo.empty());
    }
}
