package org.jys.example.common.plugin.cache;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author shenjianeng
 * @date 2020/4/4
 */
@FunctionalInterface
public interface MultiCacheLoader<K, V> {

    @NonNull
    Map<K, V> loadCache(@NonNull Collection<K> keys);

    default V loadCache(K key) {
        Map<K, V> map = loadCache(Collections.singleton(key));
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        return map.get(key);
    }
}