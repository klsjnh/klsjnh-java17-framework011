package com.klsjnh.application.export;

/*                ExportProviderRegistry class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  export provider registry class
 *
 */

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects every {@link ExportProvider} bean of the context into an
 * objectCode → provider map (immutable snapshot at startup) — the same
 * registration pattern as the scheduler JobHandlerRegistry.
 */

@Component
public class ExportProviderRegistry {

    /**
     * Object code to provider mapping.
     */
    private final Map<String, ExportProvider> providers;

    /**
     * Create the registry from all ExportProvider beans.
     *
     * @param providerBeans provider beans found in the context
     */
    public ExportProviderRegistry(List<ExportProvider> providerBeans) {
        this.providers = providerBeans.stream()
                .collect(Collectors.toUnmodifiableMap(ExportProvider::objectCode, Function.identity()));
    }

    /**
     * Resolve a provider by object code.
     *
     * @param objectCode object code
     * @return provider or null when unknown
     */
    public ExportProvider get(String objectCode) {
        return providers.get(objectCode);
    }

    /**
     * All registered object codes.
     *
     * @return object code list
     */
    public List<String> objectCodes() {
        return List.copyOf(providers.keySet());
    }
}
