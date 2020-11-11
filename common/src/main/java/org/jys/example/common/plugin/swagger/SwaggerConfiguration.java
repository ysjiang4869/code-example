package org.jys.example.common.plugin.swagger;


import lombok.extern.slf4j.Slf4j;
import org.jys.example.common.plugin.swagger.EnumModelPropertyBuilderPlugin;
import org.jys.example.common.plugin.swagger.EnumParameterBuilderPlugin;
import org.jys.example.common.plugin.swagger.LongToStringModelPropertyBuilderPlugin;
import org.jys.example.common.plugin.swagger.SwaggerAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @author shenjianeng
 * @date 2020/5/6
 */
@Slf4j
@Configuration
@Import(SwaggerAutoConfiguration.class)
public class SwaggerConfiguration {

    @Bean
    @ConditionalOnBean(Docket.class)
    public EnumModelPropertyBuilderPlugin enumModelPropertyBuilderPlugin() {
        return new EnumModelPropertyBuilderPlugin();
    }

    @Bean
    @ConditionalOnBean(Docket.class)
    public LongToStringModelPropertyBuilderPlugin longToStringModelPropertyBuilderPlugin() {
        return new LongToStringModelPropertyBuilderPlugin();
    }

    @Bean
    @ConditionalOnBean(Docket.class)
    public EnumParameterBuilderPlugin enumParameterBuilderPlugin() {
        return new EnumParameterBuilderPlugin();
    }
}

