package com.klsjnh.web.config;

/*                SpringDocConfig011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  spring doc config 011 class (framework web layer)
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;

import org.springdoc.core.models.GroupedOpenApi;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Swagger / knife4j grouping (migrated from the accumulated framework):
 * one doc group per URL namespace, plus a conditional "app" group for
 * third-party business systems that embed the framework (their own group,
 * excluding the /klsjnh/** framework paths).
 * <p>
 * Groups: system011 (system management) / storagecenter (storage center) /
 * dataservice011 (data service) / ai011 (ai model access) / lowcode011
 * (low-code core) / app (optional, yml driven). Doc meta (title / version /
 * description) is driven by {@code krt.springdoc}.
 * </p>
 */

@Configuration
@EnableConfigurationProperties(SpringDocConfig011.Properties.class)
public class SpringDocConfig011 {

    /**
     * Framework path prefix, excluded from the app group.
     */
    private static final String FRAMEWORK_PATH_PREFIX = "/klsjnh/**";

    /**
     * OpenAPI meta info, driven by krt.springdoc (title / version /
     * description).
     *
     * @param properties springdoc properties
     * @return open api info bean
     */
    @Bean
    public OpenAPI klsjnhOpenAPI(Properties properties) {
        return new OpenAPI().info(new Info()
                .title(properties.getTitle())
                .version(properties.getVersion())
                .description(properties.getDescription()));
    }

    /**
     * System management group (scheduler / user / role / audit / menu).
     *
     * @return grouped open api
     */
    @Bean
    public GroupedOpenApi system011GroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("system011")
                .displayName("系统管理011")
                .pathsToMatch("/klsjnh/system011/**")
                .addOpenApiMethodFilter(SpringDocConfig011::isDocumentedApiMethod)
                .build();
    }

    /**
     * Storage center group (endpoints land with the storage feature).
     *
     * @return grouped open api
     */
    @Bean
    public GroupedOpenApi storagecenterGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("storagecenter")
                .displayName("存储中心")
                .pathsToMatch("/klsjnh/storagecenter/**")
                .addOpenApiMethodFilter(SpringDocConfig011::isDocumentedApiMethod)
                .build();
    }

    /**
     * Data service group (datasource management; sql / model land in phase
     * two).
     *
     * @return grouped open api
     */
    @Bean
    public GroupedOpenApi dataservice011GroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("dataservice011")
                .displayName("数据服务011")
                .pathsToMatch("/klsjnh/dataservice011/**")
                .addOpenApiMethodFilter(SpringDocConfig011::isDocumentedApiMethod)
                .build();
    }

    /**
     * AI model access group (provider / api key management).
     *
     * @return grouped open api
     */
    @Bean
    public GroupedOpenApi ai011GroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("ai011")
                .displayName("AI模型接入011")
                .pathsToMatch("/klsjnh/ai011/**")
                .addOpenApiMethodFilter(SpringDocConfig011::isDocumentedApiMethod)
                .build();
    }

    /**
     * Low-code core group (object metadata, one master + three children).
     *
     * @return grouped open api
     */
    @Bean
    public GroupedOpenApi lowcode011GroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("lowcode011")
                .displayName("低代码核心011")
                .pathsToMatch("/klsjnh/lowcode011/**")
                .addOpenApiMethodFilter(SpringDocConfig011::isDocumentedApiMethod)
                .build();
    }

    /**
     * Conditional app group for third-party business systems: declare
     * {@code krt.springdoc.app.group-name} (+ optional display-name and
     * paths-to-match) in their yaml and their controllers get their own doc
     * group with the framework paths excluded.
     *
     * @param properties springdoc properties
     * @return grouped open api
     */
    @Bean
    @ConditionalOnProperty(prefix = "krt.springdoc.app", name = "group-name")
    public GroupedOpenApi appGroupedOpenApi(Properties properties) {
        Properties.App appProps = properties.getApp();

        List<String> paths = appProps.getPathsToMatch();

        if (paths == null || paths.isEmpty()) {
            paths = List.of("/**");
        }

        return GroupedOpenApi.builder()
                .group(appProps.getGroupName())
                .displayName(appProps.getDisplayName() == null ? appProps.getGroupName() : appProps.getDisplayName())
                .pathsToMatch(paths.toArray(new String[0]))
                .pathsToExclude(FRAMEWORK_PATH_PREFIX)
                .addOpenApiMethodFilter(SpringDocConfig011::isDocumentedApiMethod)
                .build();
    }

    /**
     * Whether a handler method joins the documentation: annotated with
     * {@code @Operation}, or living in a {@code @Tag} annotated class.
     *
     * @param method handler method
     * @return true when documented
     */
    public static boolean isDocumentedApiMethod(Method method) {
        if (AnnotatedElementUtils.hasAnnotation(method, Operation.class)) {
            return true;
        }

        return AnnotatedElementUtils.hasAnnotation(method.getDeclaringClass(), Tag.class);
    }

    /**
     * SpringDoc properties bound to {@code krt.springdoc} (doc meta + optional
     * app group).
     */
    @Data
    @ConfigurationProperties(prefix = "krt.springdoc")
    public static class Properties {

        /**
         * Doc title.
         */
        private String title = "klsjnh java17-framework011 API";

        /**
         * Doc version.
         */
        private String version = "1.0.0";

        /**
         * Doc description.
         */
        private String description = "klsjnh java17 framework container";

        /**
         * App group settings (third-party business systems embedding the
         * framework).
         */
        private App app = new App();

        /**
         * App group settings.
         */
        @Data
        public static class App {

            /**
             * App group name (presence activates the app group).
             */
            private String groupName;

            /**
             * App group display name, defaults to the group name.
             */
            private String displayName;

            /**
             * Paths to match, defaults to all paths (framework paths always
             * excluded).
             */
            private List<String> pathsToMatch;
        }
    }
}
