package org.jys.example.common.plugin.swagger;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.ParameterContext;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shenjianeng
 * @date 2020/4/19
 */
@SuppressWarnings(value = "all")
public class EnumParameterBuilderPlugin implements ParameterBuilderPlugin, OperationBuilderPlugin {

    private static final Joiner joiner = Joiner.on(",");

    @Override
    public void apply(ParameterContext context) {
        Class<?> type = context.resolvedMethodParameter().getParameterType().getErasedType();
        if (Enum.class.isAssignableFrom(type)) {
            SwaggerDisplayEnum annotation = AnnotationUtils.findAnnotation(type, SwaggerDisplayEnum.class);
            if (annotation != null) {

                String index = annotation.index();
                String name = annotation.name();
                Object[] enumConstants = type.getEnumConstants();
                List<String> displayValues = Arrays.stream(enumConstants).filter(Objects::nonNull).map(item -> {
                    Class<?> currentClass = item.getClass();

                    Field indexField = ReflectionUtils.findField(currentClass, index);
                    ReflectionUtils.makeAccessible(indexField);
                    Object value = ReflectionUtils.getField(indexField, item);

                    Field descField = ReflectionUtils.findField(currentClass, name);
                    ReflectionUtils.makeAccessible(descField);
                    Object desc = ReflectionUtils.getField(descField, item);
                    return value.toString();

                }).collect(Collectors.toList());

                ParameterBuilder parameterBuilder = context.parameterBuilder();
                AllowableListValues values = new AllowableListValues(displayValues, "LIST");
                parameterBuilder.allowableValues(values);
            }
        }
    }


    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    @Override
    public void apply(OperationContext context) {
        Map<String, List<String>> map = new HashMap<>();
        List<ResolvedMethodParameter> parameters = context.getParameters();
        parameters.forEach(parameter -> {
            ResolvedType parameterType = parameter.getParameterType();
            Class<?> clazz = parameterType.getErasedType();
            if (Enum.class.isAssignableFrom(clazz)) {
                SwaggerDisplayEnum annotation = AnnotationUtils.findAnnotation(clazz, SwaggerDisplayEnum.class);
                if (annotation != null) {
                    String index = annotation.index();
                    String name = annotation.name();
                    Object[] enumConstants = clazz.getEnumConstants();

                    List<String> displayValues = Arrays.stream(enumConstants).filter(Objects::nonNull).map(item -> {
                        Class<?> currentClass = item.getClass();

                        Field indexField = ReflectionUtils.findField(currentClass, index);
                        ReflectionUtils.makeAccessible(indexField);
                        Object value = ReflectionUtils.getField(indexField, item);

                        Field descField = ReflectionUtils.findField(currentClass, name);
                        ReflectionUtils.makeAccessible(descField);
                        Object desc = ReflectionUtils.getField(descField, item);
                        return value + ":" + desc;

                    }).collect(Collectors.toList());

                    map.put(parameter.defaultName().orElse(""), displayValues);

                    OperationBuilder operationBuilder = context.operationBuilder();
                    Field parametersField = ReflectionUtils.findField(operationBuilder.getClass(), "parameters");
                    ReflectionUtils.makeAccessible(parametersField);
                    List<Parameter> list = (List<Parameter>) ReflectionUtils.getField(parametersField, operationBuilder);

                    map.forEach((k, v) -> {
                        for (Parameter currentParameter : list) {
                            if (StringUtils.equals(currentParameter.getName(), k)) {
                                Field description = ReflectionUtils.findField(currentParameter.getClass(), "description");
                                ReflectionUtils.makeAccessible(description);
                                Object field = ReflectionUtils.getField(description, currentParameter);
                                ReflectionUtils.setField(description, currentParameter, field + " , " + joiner.join(v));
                                break;
                            }
                        }
                    });
                }
            }
        });
    }
}