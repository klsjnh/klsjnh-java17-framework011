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
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
 * Path-based groups live here but are <b>conditional on the owning center</b>:
 * a group is registered only when the matching {@code @EnableXxx011Center}
 * imported the center's auto configuration, so a center that is not enabled
 * has no group at all (not an empty group) in the knife4j / swagger-config
 * list. {@code iam} and the optional {@code app} group stay unconditional
 * because core's own IAM endpoints are always present. Doc meta is driven by
 * {@code krt.springdoc}.
 * </p>
 * <p>
 * The conditions are evaluated while the auto configuration is parsed (this
 * class is scanned from {@code CoreAutoConfiguration011}), i.e. after the
 * {@code @EnableXxx011Center} imports carried by the host's configuration
 * class — the marker beans are already registered by then, so matching by
 * type name is stable.
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
     * Marker bean (type name) registered only by {@code @EnablePlatform011Center}
     * — the system011 group follows that center.
     */
    private static final String PLATFORM_CENTER = "com.klsjnh.enabled.PlatformCenterAutoConfiguration011";

    /**
     * Marker bean (type name) registered only by {@code @EnableStorage011Center}.
     */
    private static final String STORAGE_CENTER = "com.klsjnh.enabled.StorageCenterAutoConfiguration011";

    /**
     * Marker bean (type name) registered only by {@code @EnableDatasource011Center}.
     */
    private static final String DATASOURCE_CENTER = "com.klsjnh.enabled.DatasourceCenterAutoConfiguration011";

    /**
     * Marker bean (type name) registered only by {@code @EnableAi011Center}.
     */
    private static final String AI_CENTER = "com.klsjnh.enabled.AiCenterAutoConfiguration011";

    /**
     * Marker bean (type name) registered only by {@code @EnableMessage011Center}.
     */
    private static final String MESSAGE_CENTER = "com.klsjnh.enabled.MessageCenterAutoConfiguration011";

    /**
     * OpenAPI meta info, driven by krt.springdoc (title / version /
     * description).
     *
     * @param properties springdoc properties
     * @return open api info bean
     */
    @Bean
    public OpenAPI klsjnhOpenAPI(Properties properties) {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Bearer <JWT>（登录后取 token；doc.html 右上角 Authorize 填入）");

        return new OpenAPI()
                .info(new Info()
                        .title(properties.getTitle())
                        .version(properties.getVersion())
                        .description(properties.getDescription()))
                .components(new Components().addSecuritySchemes("bearerAuth", bearer))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * System management group (config / dictionary from the platform center;
     * scheduler joins through the platform center scan when the quartz starter
     * is present). Registered only when {@code @EnablePlatform011Center} is
     * used.
     *
     * @return grouped open api
     */
    @Bean
    @ConditionalOnBean(type = PLATFORM_CENTER)
    public GroupedOpenApi system011GroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("system011")
                .displayName("系统管理011")
                .pathsToMatch("/klsjnh/system011/**")
                .addOpenApiMethodFilter(SpringDocConfig011::isDocumentedApiMethod)
                .build();
    }

    /**
     * IAM group. Unconditional: core always ships the user / audit endpoints
     * (the access center, when enabled, adds its {@code iam.access} paths to
     * the same group).
     *
     * @return grouped open api
     */
    @Bean
    public GroupedOpenApi iamGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("iam")
                .displayName("IAM011")
                .pathsToMatch("/klsjnh/iam/**")
                .addOpenApiMethodFilter(SpringDocConfig011::isDocumentedApiMethod)
                .build();
    }

    /**
     * Storage center group. Registered only when
     * {@code @EnableStorage011Center} is used (directly or via
     * {@code @EnableAi011Center}).
     *
     * @return grouped open api
     */
    @Bean
    @ConditionalOnBean(type = STORAGE_CENTER)
    public GroupedOpenApi storagecenterGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("storagecenter")
                .displayName("存储中心011")
                .pathsToMatch("/klsjnh/storagecenter/**")
                .addOpenApiMethodFilter(SpringDocConfig011::isDocumentedApiMethod)
                .build();
    }

    /**
     * Data service group (datasource management; sql / sync join it).
     * Registered only when {@code @EnableDatasource011Center} is used.
     *
     * @return grouped open api
     */
    @Bean
    @ConditionalOnBean(type = DATASOURCE_CENTER)
    public GroupedOpenApi datasourceGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("datasource")
                .displayName("数据源011")
                .pathsToMatch("/klsjnh/datasource/**")
                .addOpenApiMethodFilter(SpringDocConfig011::isDocumentedApiMethod)
                .build();
    }

    /**
     * AI model access group (provider / api key management). Registered only
     * when {@code @EnableAi011Center} is used.
     *
     * @return grouped open api
     */
    @Bean
    @ConditionalOnBean(type = AI_CENTER)
    public GroupedOpenApi aicenterGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("aicenter")
                .displayName("AI中心011")
                .pathsToMatch("/klsjnh/aicenter/**")
                .addOpenApiMethodFilter(SpringDocConfig011::isDocumentedApiMethod)
                .build();
    }

    /**
     * Message center group (channel / template config plus the send records).
     * Registered only when {@code @EnableMessage011Center} is used.
     *
     * @return grouped open api
     */
    @Bean
    @ConditionalOnBean(type = MESSAGE_CENTER)
    public GroupedOpenApi messagecenterGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("messagecenter")
                .displayName("消息中心011")
                .pathsToMatch("/klsjnh/messagecenter/**")
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
