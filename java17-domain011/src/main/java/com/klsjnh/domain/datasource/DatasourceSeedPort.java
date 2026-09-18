package com.klsjnh.domain.datasource;

/*                DatasourceSeedPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  datasource seed port interface
 *
 */

import com.klsjnh.domain.datasource.ConnectionInfo;

import java.util.List;

/**
 * Bootstrap seed port: materializes the yaml list ({@code krt.ci011}) into the
 * july_datasource table exactly once per unknown dsCode.
 * <p>
 * The port exists so the seed can be triggered from the infrastructure layer
 * (the startup runner) without that layer reaching up into the application
 * layer — the dependency direction stays domain ← infrastructure, as everywhere
 * else in this project.
 * </p>
 */

public interface DatasourceSeedPort {

    /**
     * Seed the table from the bootstrap list.
     * <p>
     * Per entry, in order of precedence: a dsCode the table has never seen is
     * inserted; a dsCode that exists — <b>including a logic-deleted one</b> — is
     * skipped and never updated or revived; a stored row whose content differs
     * from the yaml keeps the stored value and is reported as a WARN.
     * </p>
     * <p>
     * Must never throw for a bad entry: the caller is a startup runner and the
     * failure policy is "log at ERROR and carry on". Only an unexpected
     * infrastructure fault may propagate.
     * </p>
     *
     * @param fromYaml bootstrap configs, null or empty means nothing to do
     * @return seed summary, never null
     */
    SeedResult011 seed(List<ConnectionInfo> fromYaml);
}
