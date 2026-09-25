package com.klsjnh.application.platform011.importdata;

/*                ImportProviderRegistry class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  import provider registry class
 *
 */

import com.klsjnh.domain.platform011.importdata.ImportProvider;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects every {@link ImportProvider} bean into an objectCode map.
 */

@Component
public class ImportProviderRegistry {

    /**
     * Object code to provider mapping.
     */
    private final Map<String, ImportProvider> providers;

    /**
     * Create the registry.
     *
     * @param providerBeans provider beans
     */
    public ImportProviderRegistry(List<ImportProvider> providerBeans) {
        this.providers = providerBeans.stream()
                .collect(Collectors.toUnmodifiableMap(ImportProvider::objectCode, Function.identity()));
    }

    /**
     * Resolve a provider by object code.
     *
     * @param objectCode object code
     * @return provider or null
     */
    public ImportProvider get(String objectCode) {
        return providers.get(objectCode);
    }
}
