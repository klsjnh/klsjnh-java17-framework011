package com.klsjnh.domain.datasource;

/*                ConnectionInfo record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  connection info record
 *
 */

/**
 * Dynamic datasource connection config (krt.ci011 entry): one business
 * database target addressable by its unique dsCode.
 *
 * @param dsCode   datasource code, unique
 * @param dsName   datasource name, display only
 * @param dsType   database type (mysql / postgresql / oracle / sqlserver)
 * @param dsUrl    jdbc url
 * @param username login user
 * @param password login password
 */

public record ConnectionInfo(String dsCode, String dsName, String dsType, String dsUrl, String username,
        String password) {
}
