package org.jys.example.common.redis.bloomfilter;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YueSong Jiang
 * @date 2019/3/15
 * @description <p> </p>
 */
public class BloomFilterRedis<T> implements BloomFilter<T> {
    private final StringRedisTemplate template;
    private final FilterBuilder config;
    private final String name;
    private final int hashNum;

    public BloomFilterRedis(FilterBuilder builder) {
        builder.complete();
        RedisKeys keys = new RedisKeys(builder.name());
        this.template = builder.template();
        name = keys.getBitsKey();
        this.config = keys.persistConfig(builder);
        hashNum=config.hashes();
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
    public  List<Boolean> addAll(Collection<T> elements) {
        List<Object> results = template.executePipelined((RedisCallback<Boolean>) redisConnection -> {
            StringRedisConnection stringCoon = (StringRedisConnection) redisConnection;
            for(T element:elements){
                Arrays.stream(hash(toBytes(element))).forEach(p -> stringCoon.setBit(name, p, true));
            }
            return null;
        });
        return mergeMultiResults(results,hashNum,elements.size());
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
        List<Object> results = template.execute(new SessionCallback<List<Object>>() {
            // use redis transaction
            @Override
            public <K, V> List<Object> execute(@Nullable RedisOperations<K, V> redisOperations) throws DataAccessException {
                if(Objects.isNull(redisOperations)){
                    throw new NullPointerException("redis operation is null");
                }
                redisOperations.multi();
                Arrays.stream(hash(element)).forEach(index -> template.opsForValue().getBit(name, index));
                return redisOperations.exec();
            }
        });
        if (results == null || results.isEmpty()) {
            return false;
        }
        return results.stream().allMatch(b -> Optional.ofNullable((Boolean)b).orElse(false));
    }

    @Override
    public List<Boolean> contains(Collection<T> elements) {
        List<Object> results = template.execute(new SessionCallback<List<Object>>() {
            // use redis transaction
            @Override
            public <K, V> List<Object> execute(@Nullable RedisOperations<K, V> redisOperations) throws DataAccessException {
                if(Objects.isNull(redisOperations)){
                    throw new NullPointerException("redis operation is null");
                }
                redisOperations.multi();
                for(T element:elements){
                    Arrays.stream(hash(toBytes(element))).forEach(index -> template.opsForValue().getBit(name, index));
                }
                List<Object> multiResult=redisOperations.exec();
                return mergeMultiResults(multiResult,hashNum,elements.size()).stream().map(x->(Object)x).collect(Collectors.toList());
            }
        });
        if(results==null){
            return Collections.emptyList();
        }
        return results.stream().map(x->(Boolean)x).collect(Collectors.toList());
    }

    @Override
    public boolean containsAll(Collection<T> elements) {
        return contains(elements).stream().allMatch(b -> Optional.ofNullable(b).orElse(false));
    }

    private List<Boolean> mergeMultiResults(List<Object> result, int hashNum, int elementSize){
        if(elementSize*hashNum!=result.size()){
            throw new RuntimeException("result size is not right");
        }
        Boolean[] ret=new Boolean[elementSize];
        for (int i = 0; i < elementSize; i++) {
            ret[i]=true;
            for (int j = 0; j < hashNum; j++) {
                if(!(boolean)(result.get(i*hashNum+j))){
                    ret[i]=false;
                    break;
                }
            }
        }
        return Arrays.asList(ret);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BloomFilterRedis<?> that = (BloomFilterRedis<?>) o;
        return config != null ? (config.isCompatibleTo(that.config)) : (that.config == null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(template, config, name);
    }
}
