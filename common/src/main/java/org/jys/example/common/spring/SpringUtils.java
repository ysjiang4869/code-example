package org.jys.example.common.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

/**
 * @author YueSong Jiang
 * @date 2019/3/15
 * Get spring bean by class or bean name
 * You are not recommended to use this method, because you can't be sure this will be initialized before use
 * You should implements the ApplicationContextAware if any class need, or not use static method in this class
 * and wired bean when need
 */
@Component
public class SpringUtils implements EmbeddedValueResolverAware, ApplicationContextAware {


    private static ApplicationContext applicationContext;
    private static StringValueResolver stringValueResolver;
    private static final String KEY_FORMAT = "${%s}";

    @Override
    public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
        if (SpringUtils.stringValueResolver == null) {
            SpringUtils.stringValueResolver = stringValueResolver;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringUtils.applicationContext == null) {
            SpringUtils.applicationContext = applicationContext;
        }
    }

    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> aClass) {
        return applicationContext.getBean(aClass);
    }

    public static <T> T getBean(String name, Class<T> aClass) {
        return applicationContext.getBean(name, aClass);
    }

    public static String getProperty(String key) {
        return stringValueResolver.resolveStringValue(String.format(KEY_FORMAT, key));
    }
}
