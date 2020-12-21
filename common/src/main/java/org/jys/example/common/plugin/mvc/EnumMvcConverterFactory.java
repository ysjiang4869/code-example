package org.jys.example.common.plugin.mvc;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * springMVC 枚举类的转换器
 * 如果枚举类中有工厂方法(静态方法)被标记为{@link EnumConvertMethod },则调用该方法转为枚举对象
 *
 * @author jiangyuesong
 * @date 2020/4/19
 */
@SuppressWarnings("all")
public class EnumMvcConverterFactory implements ConverterFactory<String, Enum<?>> {

    private final ConcurrentMap<Class<? extends Enum<?>>, EnumMvcConverterHolder> holderMapper = new ConcurrentHashMap<>();


    @Override
    public <T extends Enum<?>> Converter<String, T> getConverter(Class<T> targetType) {
        EnumMvcConverterHolder holder = holderMapper.computeIfAbsent(targetType, EnumMvcConverterHolder::createHolder);
        return (Converter<String, T>) holder.converter;
    }


    @AllArgsConstructor
    static class EnumMvcConverterHolder {
        @Nullable
        final EnumMvcConverter<?> converter;

        static EnumMvcConverterHolder createHolder(Class<?> targetType) {
            List<Method> methodList = MethodUtils.getMethodsListWithAnnotation(targetType, EnumConvertMethod.class, false, true);
            if (CollectionUtils.isEmpty(methodList)) {
                return new EnumMvcConverterHolder(null);
            }
            Assert.isTrue(methodList.size() == 1, "@EnumConvertMethod 只能标记在一个工厂方法(静态方法)上");
            Method method = methodList.get(0);
            Assert.isTrue(Modifier.isStatic(method.getModifiers()), "@EnumConvertMethod 只能标记在工厂方法(静态方法)上");
            return new EnumMvcConverterHolder(new EnumMvcConverter<>(method));
        }

    }

    static class EnumMvcConverter<T extends Enum<T>> implements Converter<String, T> {

        private final Method method;

        public EnumMvcConverter(Method method) {
            this.method = method;
            this.method.setAccessible(true);
        }

        @Override
        public T convert(String source) {
            if (source.isEmpty()) {
                // reset the enum value to null.
                return null;
            }
            try {
                return (T) method.invoke(null, Integer.valueOf(source));
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

    }


}
