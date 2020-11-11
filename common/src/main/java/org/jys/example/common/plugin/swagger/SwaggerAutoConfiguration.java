package org.jys.example.common.plugin.swagger;

import com.google.common.collect.ImmutableSet;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * copy from https://github.com/SpringForAll/spring-boot-starter-swagger
 */
@Slf4j
@Configuration
@EnableSwagger2
@ConditionalOnProperty(name = "swagger.enabled", matchIfMissing = false)
public class SwaggerAutoConfiguration {

    private static final Set<String> DEFAULT_IGNORED_PARAMETER_TYPES =
            ImmutableSet.of(
                    "com.dxy.platform.chd.commons.session.UserSession",
                    "javax.servlet.http.HttpServletRequest",
                    "javax.servlet.http.HttpServletResponse");


    @Bean
    @ConditionalOnMissingBean
    public SwaggerProperties swaggerProperties() {
        return new SwaggerProperties();
    }

    @Bean
    public Docket createRestApi(SwaggerProperties swaggerProperties) {
        // 没有分组
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .version(swaggerProperties.getVersion())
                .build();

        // base-path处理
        // 当没有配置任何path的时候，解析/**
        if (swaggerProperties.getBasePath().isEmpty()) {
            swaggerProperties.getBasePath().add("/**");
        }
        List<Predicate<String>> basePath = new ArrayList<>();
        for (String path : swaggerProperties.getBasePath()) {
            basePath.add(PathSelectors.ant(path));
        }

        // exclude-path处理
        List<Predicate<String>> excludePath = new ArrayList<>();
        for (String path : swaggerProperties.getExcludePath()) {
            excludePath.add(PathSelectors.ant(path));
        }

        Docket docketForBuilder = new Docket(DocumentationType.SWAGGER_2)
                .host(swaggerProperties.getHost())
                .apiInfo(apiInfo);

        Docket docket = docketForBuilder.select()
                // 只支持 @ApiOperation 注解标注的方法
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(excludePath.stream().reduce(x -> false, Predicate::or).negate()
                        .and(basePath.stream().reduce(x -> false, Predicate::or))
                ).build();

        /* ignoredParameterTypes **/
        Class<?>[] array = new Class[swaggerProperties.getIgnoredParameterTypes().size()];
        Class<?>[] ignoredParameterTypes = swaggerProperties.getIgnoredParameterTypes().toArray(array);
        docket.ignoredParameterTypes(ignoredParameterTypes);

        addDefaultIgnoredParameterTypes(docket);

        return docket;

    }

    private void addDefaultIgnoredParameterTypes(Docket docket) {
        DEFAULT_IGNORED_PARAMETER_TYPES.forEach(className -> {
            try {
                docket.ignoredParameterTypes(ClassUtils.forName(className, SwaggerAutoConfiguration.class.getClassLoader()));
                log.info("addDefaultIgnoredParameterTypes : " + className);
            } catch (ClassNotFoundException ex) {
                // ignore
            }
        });
    }
}
