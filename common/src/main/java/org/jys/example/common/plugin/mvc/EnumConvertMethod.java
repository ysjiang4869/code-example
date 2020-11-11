package org.jys.example.common.plugin.mvc;

import java.lang.annotation.*;

/**
 * 在自定义枚举类的工厂方法上标记该注解,用于Spring MVC 转换器转换枚举
 * {@link EnumMvcConverterFactory}
 *
 * @author shenjianeng
 * @date 2020/4/19
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnumConvertMethod {
}
