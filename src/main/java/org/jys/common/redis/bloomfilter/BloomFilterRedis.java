package org.jys.common.redis.bloomfilter;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;

/**
 * @author YueSong Jiang
 * @date 2019/3/15
 * @description <p> </p>
 */
public class BloomFilterRedis<T> implements BloomFilter<T> {
    private final StringRedisTemplate template;
    private final FilterBuilder config;
    private final String name;

    public BloomFilterRedis(FilterBuilder builder) {
        builder.complete();
        RedisKeys keys = new RedisKeys(builder.name());
        this.template = builder.template();
        name = keys.getBitsKey();
        this.config = keys.persistConfig(builder);
    }

    @Override
    public boolean addRaw(byte[] element) {
        List<Object> results = template.executePipelined((RedisCallback<Object>) redisConnection -> {
            StringRedisConnection stringCoon = (StringRedisConnection) redisConnection;
            Arrays.stream(hash(element)).forEach(p -> stringCoon.setBit(name, p, true));
            return null;
        });
        return results.stream().allMatch(b -> (Boolean) b);
    }

    @Override
    public void clear() {
        template.delete(name);
    }

    @Override
    public void remove() {
        clear();
        template.delete(config.name());
    }

    @Override
    public boolean contains(byte[] element) {
        List<Boolean> results = template.execute(new SessionCallback<List<Boolean>>() {
            @Override
            public <K, V> List<Boolean> execute(RedisOperations<K, V> redisOperations) throws DataAccessException {
                Arrays.stream(hash(element)).forEach(index -> template.opsForValue().getBit(name, index));
                return Collections.emptyList();
            }
        });
        if (results == null || results.isEmpty()) {
            return false;
        }
        return results.stream().allMatch(b -> Optional.ofNullable(b).orElse(false));
    }

    @Override
    public FilterBuilder config() {
        return config;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean union(BloomFilter<T> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean intersect(BloomFilter<T> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        Boolean result =
                template.execute((RedisCallback<Boolean>) redisConnection -> ((StringRedisConnection) redisConnection).bitCount(name) == 0);
        return Optional.ofNullable(result).orElse(false);
    }

    @Override
    public Double getEstimatedPopulation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BloomFilterRedis<?> that = (BloomFilterRedis<?>) o;
        return config != null ? (config.isCompatibleTo(that.config)) : (that.config == null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(template, config, name);
    }
}
