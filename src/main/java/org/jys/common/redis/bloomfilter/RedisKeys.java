package org.jys.common.redis.bloomfilter;


import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Encapsulates the Redis keys for the Redis Bloom Filters
 */
public class RedisKeys {

    //Redis key constants
    public static final String N_KEY = "n";
    public static final String M_KEY = "m";
    public static final String K_KEY = "k";
    public static final String C_KEY = "c";
    public static final String P_KEY = "p";
    public static final String HASH_METHOD_KEY = "hashmethod";
    public final String BITS_KEY;
    public final String COUNTS_KEY;
    public final String TTL_KEY;
    public final String EXPIRATION_QUEUE_KEY;

    public RedisKeys(String instanceName) {
        this.BITS_KEY = instanceName + ":bits";
        this.COUNTS_KEY = instanceName + ":counts";
        this.TTL_KEY = instanceName + ":ttl";
        this.EXPIRATION_QUEUE_KEY = instanceName + ":queue";
    }

    public String getBitsKey() {
        return BITS_KEY;
    }

    public FilterBuilder persistConfig(FilterBuilder builder) {
        FilterBuilder newConfig = null;
        StringRedisTemplate template = builder.template();
        Boolean hasKey = template.hasKey(builder.name());
        boolean exist = Optional.ofNullable(hasKey).orElse(false);
        while (newConfig == null) {
            if (!builder.overwriteIfExists() && exist) {
                Map<Object, Object> data = template.opsForHash().entries(builder.name());
                Map<String, String> mapConfig = data.entrySet().stream().
                        collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
                newConfig = parseConfigHash(mapConfig, builder.name());
                if (!newConfig.isCompatibleTo(builder)) {
                    newConfig = null;
                    builder.overwriteIfExists(true);
                }
            } else {
                Map<String, String> hash = buildConfigHash(builder);
                template.opsForHash().putAll(builder.name(), hash);
                newConfig = builder;
            }
        }
        return newConfig;
    }

    public Map<String, String> buildConfigHash(FilterBuilder config) {
        Map<String, String> map = new HashMap<>();
        map.put(P_KEY, String.valueOf(config.falsePositiveProbability()));
        map.put(M_KEY, String.valueOf(config.size()));
        map.put(K_KEY, String.valueOf(config.hashes()));
        map.put(N_KEY, String.valueOf(config.expectedElements()));
        map.put(C_KEY, String.valueOf(config.countingBits()));
        map.put(HASH_METHOD_KEY, config.hashMethod().name());
        return map;
    }

    public FilterBuilder parseConfigHash(Map<String, String> map, String name) {
        FilterBuilder config = new FilterBuilder();
        config.name(name);
        config.falsePositiveProbability(Double.valueOf(map.get(P_KEY)));
        config.size(Integer.valueOf(map.get(M_KEY)));
        config.hashes(Integer.valueOf(map.get(K_KEY)));
        config.expectedElements(Integer.valueOf(map.get(N_KEY)));
        config.countingBits(Integer.valueOf(map.get(C_KEY)));
        config.hashFunction(HashProvider.HashMethod.valueOf(map.get(HASH_METHOD_KEY)));
        config.complete();
        return config;
    }

}
