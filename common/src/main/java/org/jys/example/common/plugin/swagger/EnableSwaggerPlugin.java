package org.jys.example.common.plugin.swagger;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author shenjianeng
 * @date 2020/5/8
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SwaggerConfiguration.class)
public @interface EnableSwaggerPlugin {
}
