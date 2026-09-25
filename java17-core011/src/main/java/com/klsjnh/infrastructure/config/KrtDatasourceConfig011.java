package com.klsjnh.infrastructure.config;

/*                KrtDatasourceConfig011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  datasource-side krt config (split from KrtConfig011)
 *
 */

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import com.klsjnh.domain.datasource.kernel.ConnectionInfo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Datasource-side framework config bound to the {@code krt.*} keys: the
 * dynamic datasource connection list. Lives in core so the dynamic datasource
 * kernel ({@code java17-data-mybatis011-starter}) and the optional datasource
 * center seed runner can both consume it through the core dependency.
 */

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "krt")
public class KrtDatasourceConfig011 {

    /**
     * Dynamic datasource connection list (krt.ci011) — declared at startup,
     * pools connect lazily on first routing call.
     */
    private List<ConnectionInfo> ci011 = new ArrayList<>();
}
