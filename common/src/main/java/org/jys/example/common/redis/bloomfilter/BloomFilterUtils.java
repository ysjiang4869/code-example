package org.jys.example.common.redis.bloomfilter;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author YueSong Jiang
 * @date 2019/3/15
 * Common tools for bloom filter
 */
public class BloomFilterUtils {

    private BloomFilterUtils() {
    }

    /**
     * delete no use keys when update redis bloom filter
     * @param redisTemplate spring redis template
     * @param keyFormat the bloom filter key prefix
     * @param num the first num need to be delete
     */
    private static void deleteDeprecatedKeys(StringRedisTemplate redisTemplate,
                                             String keyFormat, int num) {
        boolean hasDeprecatedKey = true;
        int i = num;
        String key;
        while (hasDeprecatedKey) {
            key = keyFormat + i;
            Boolean b = redisTemplate.hasKey(key);
            hasDeprecatedKey = Optional.ofNullable(b).orElse(false);
            redisTemplate.delete(key);
            i++;
        }
    }

    /**
     * create the bloom filters
     * this will create multi many bloom-filter if the expectElements is too large
     * @param expectElements the number of element need to filter
     * @param falsePositive the false positive
     * @param keyFormat the bloom filter key prefix
     * @param redisTemplate spring redis template
     * @return multi bloom-filter with num 0-N
     */
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
        BloomFilterUtils.deleteDeprecatedKeys(redisTemplate, keyFormat, num);
        return bloomFilterHashMap;
    }
}
