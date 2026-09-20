package com.klsjnh.domain.datasource.sync;

/*                SyncEnginePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync engine port
 *
 */

/**
 * Sync engine port: orchestrate source read → column mapping / type coercion →
 * target write, page by page, for one rule.
 */

public interface SyncEnginePort {

    /**
     * Run a sync rule once.
     *
     * @param rule the rule aggregate (source / target / key / conflict / columns)
     * @return run result
     */
    SyncRunResult run(JulySyncRule rule);
}
