package org.jys.common.redis.bloomfilter;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author YueSong Jiang
 * @date 2019/3/15
 * @description <p> </p>
 */
public class BloomFilterUtils {

    private BloomFilterUtils() {
    }

    private static void deleteDeprecatedkeys(StringRedisTemplate redisTemplate,
                                             String keyFormat, int num) {
        boolean hasDeprecatedkey = true;
        int i = num;
        String key;
        while (hasDeprecatedkey) {
            key = keyFormat + i;
            Boolean b = redisTemplate.hasKey(key);
            hasDeprecatedkey = Optional.ofNullable(b).orElse(false);
            redisTemplate.delete(key);
            i++;
        }
    }

    public static Map<Integer, BloomFilter<String>> createBloomFilter(long expectElements, double falsePositive,
                                                                      String keyFormat,
                                                                      StringRedisTemplate redisTemplate) {
        long originSize = FilterBuilder.optimalM(expectElements, falsePositive);
        int num = (int) (originSize / Integer.MAX_VALUE + 1);
        Map<Integer, BloomFilter<String>> bloomFilterHashMap = new HashMap<>(num);
        int realExpectElements = (int) (expectElements / num);
        String key;
        for (int i = 0; i < num; i++) {
            key = keyFormat + i;
            FilterBuilder filterBuilder = new FilterBuilder().name(key)
                    .falsePositiveProbability(falsePositive)
                    .expectedElements(realExpectElements)
                    .template(redisTemplate).complete();
            BloomFilter<String> bloomFilter = new BloomFilterRedis<>(filterBuilder);
            if (bloomFilter.config().overwriteIfExists()) {
                bloomFilter.clear();
            }
            String filterInfo = bloomFilter.asString();
            System.out.println(filterInfo);
            bloomFilterHashMap.put(i, bloomFilter);
        }
        BloomFilterUtils.deleteDeprecatedkeys(redisTemplate, keyFormat, num);
        return bloomFilterHashMap;
    }
}
