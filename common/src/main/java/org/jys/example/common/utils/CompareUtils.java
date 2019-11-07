package org.jys.example.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * @author YueSong Jiang
 * @date 2017/10/10
 * @description <p> </p>
 */
public class CompareUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Compare two object and return modified fields which contain in comparedProperties
     *
     * @param source             source object
     * @param target             target object
     * @param comparedProperties the fields need to be compare
     *                           null means to compare all
     * @return the modified fields and value after modify
     */
    public static Map<String, Object> getModifyContent(Object source, Object target, Set<String> comparedProperties) {
        Map<String, Object> modifies = new HashMap<>();
          /*
          process null problem, if all null means equal
          if only source is null means all modified
          if only target is null means nothing changed
         */
        if (null == source || null == target) {
            if (null == source && null == target) return modifies;
            else if (null == target) return modifies;
            else {
                return mapper.convertValue(target, new TypeReference<Map<String, Object>>() {
                });
            }
        }
        // source and target must be same class type
        if (!Objects.equals(source.getClass().getName(), target.getClass().getName())) {
            throw new ClassCastException("source and target are not same class type");
        }
        Map<String, Object> sourceMap = mapper.convertValue(source, new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> targetMap = mapper.convertValue(target, new TypeReference<Map<String, Object>>() {
        });

        sourceMap.forEach((k, v) -> {
            if (comparedProperties != null && !comparedProperties.contains(k)) {
                return;
            }
            Object targetValue = targetMap.get(k);
            if (!Objects.equals(v, targetValue)) {
                modifies.put(k, targetValue);
            }
        });
        return modifies;
    }

    /**
     * Compare two object and return if equal
     *
     * @param source             source object
     * @param target             target object
     * @param comparedProperties only compare fields in this map
     *                           null means to compare all
     * @return if equal
     */
    public static boolean isEuqal(Object source, Object target, Set<String> comparedProperties) {
        if (null == source || null == target) {
            return null == source && null == target;
        }
        if (!Objects.equals(source.getClass().getName(), target.getClass().getName())) {
            return false;
        }
        Map<String, Object> sourceMap = mapper.convertValue(source, new TypeReference<Map<String, Object>>() {
        });
        Map<String, Object> targetMap = mapper.convertValue(target, new TypeReference<Map<String, Object>>() {
        });

        for (String k : sourceMap.keySet()) {
            if (comparedProperties != null && !comparedProperties.contains(k)) {
                continue;
            }
            Object v = sourceMap.get(k);
            Object targetValue = targetMap.get(k);
            if (!Objects.equals(v, targetValue)) {
                return false;
            }
        }
        return true;
    }
}
