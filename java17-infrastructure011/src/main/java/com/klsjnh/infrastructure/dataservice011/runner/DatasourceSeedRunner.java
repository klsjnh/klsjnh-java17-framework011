package com.klsjnh.infrastructure.dataservice011.runner;

/*                DatasourceSeedRunner class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  datasource seed runner class
 *
 */

import lombok.extern.slf4j.Slf4j;

import com.klsjnh.domain.dataservice011.DatasourceSeedPort;
import com.klsjnh.domain.dataservice011.SeedResult011;
import com.klsjnh.domain.datasource.ConnectionInfo;
import com.klsjnh.domain.datasource.DynamicDataSourceRegistryPort;
import com.klsjnh.domain.datasource.ReloadResult;

import com.klsjnh.infrastructure.config.KrtConfig011;
import com.klsjnh.infrastructure.dataservice011.repository.JulyDatasourceRepositoryImpl;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Startup seed runner: materializes the yaml bootstrap list ({@code krt.ci011})
 * into the july_datasource table, then refreshes the dynamic datasource
 * registry so the runtime serves exactly the table content.
 * <p>
 * <b>Why a runner and not the registry constructor:</b> the registry is built
 * while the context wires beans, at which point the table is not reachable yet.
 * Seeding and reloading are post-context work — the context is fully started,
 * MyBatis-Plus is usable and the repositories are injectable.
 * </p>
 * <p>
 * <b>Failure policy (product decision):</b> the runner NEVER aborts startup. A
 * seed entry that cannot be inserted is logged at ERROR and skipped, and the
 * registry reload is attempted regardless, so the service still boots and an
 * operator can open the management page to repair the data. Aborting the boot
 * would take away the very page needed to diagnose the fault.
 * </p>
 * <p>
 * The runner talks to domain ports only, never to the application layer, so it
 * stays a framework capability owned by infrastructure.
 * </p>
 */

@Slf4j
@Component
@Order(DatasourceSeedRunner.SEED_ORDER)
public class DatasourceSeedRunner implements ApplicationRunner {

    /**
     * Run order: after the framework essential runners, before application
     * smoke runners.
     */
    public static final int SEED_ORDER = 100;

    /**
     * Framework config holding the yaml bootstrap list.
     */
    private final KrtConfig011 krtConfig;

    /**
     * Bootstrap seed port.
     */
    private final DatasourceSeedPort seedPort;

    /**
     * Dynamic datasource registry (declaration + reload).
     */
    private final DynamicDataSourceRegistryPort registry;

    /**
     * Repository used to read the runtime snapshot for the reload.
     */
    private final JulyDatasourceRepositoryImpl repository;

    /**
     * Create the runner.
     *
     * @param krtConfig  framework config
     * @param seedPort   bootstrap seed port
     * @param registry   dynamic datasource registry
     * @param repository july datasource repository
     */
    public DatasourceSeedRunner(KrtConfig011 krtConfig, DatasourceSeedPort seedPort,
            DynamicDataSourceRegistryPort registry, JulyDatasourceRepositoryImpl repository) {
        this.krtConfig = krtConfig;
        this.seedPort = seedPort;
        this.registry = registry;
        this.repository = repository;
    }

    /**
     * Seed the table from the yaml bootstrap list, then reload the registry.
     * <p>
     * Both steps are individually guarded: a seed failure still triggers the
     * reload (the table content is what matters at runtime), and a reload
     * failure does not fail the boot either — the registry keeps the yaml
     * baseline it declared at construction time and the operator sees the ERROR
     * in the log.
     * </p>
     *
     * @param args application arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            SeedResult011 seed = seedPort.seed(krtConfig.getCi011());

            log.info("yaml seed done total {} inserted {} skipped {} failed {} ...",
                    seed.total(), seed.inserted(), seed.skipped(), seed.failed());
        } catch (Exception ex) {
            log.error("yaml seed aborted, registry reload will still run from the current table content", ex);
        }

        try {
            ReloadResult reload = registry.reloadAll(enabledConfigs());

            log.info("registry reload after seed enabled {} registered {} reused {} closed {} failed {} ...",
                    reload.enabled(), reload.registered(), reload.reused(), reload.closed(), reload.failed());
        } catch (Exception ex) {
            log.error("registry reload after seed failed, the runtime keeps the yaml baseline configs", ex);
        }
    }

    /**
     * Build the registry snapshot from the enabled table rows.
     *
     * @return connection infos, never null
     */
    private List<ConnectionInfo> enabledConfigs() {
        List<ConnectionInfo> infos = new ArrayList<>();

        repository.findAllEnabled().forEach(datasource -> infos.add(new ConnectionInfo(datasource.dsCode(),
                datasource.dsName(), datasource.dbType(), datasource.jdbcUrl(), datasource.username(),
                datasource.password(), datasource.schemaName(), datasource.driverClass(),
                datasource.poolConfig())));

        return infos;
    }
}
