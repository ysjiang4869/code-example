package org.jys.example.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jys.example.common.redis.bloomfilter.BloomFilter;
import org.jys.example.common.redis.bloomfilter.BloomFilterRedis;
import org.jys.example.common.redis.bloomfilter.FilterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author YueSong Jiang
 * @date 2019/11/1
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class BloomFilterRedisTest {

    @Autowired
    @Qualifier("bloomFilterRedisTemplate")
    public StringRedisTemplate redisTemplate;

    @Test
    public void addTest(){

        FilterBuilder test=new FilterBuilder().expectedElements(10_000)
                .falsePositiveProbability(0.01).name("test1")
                .template(redisTemplate);
        BloomFilter<String> bloomFilter=new BloomFilterRedis<>(test);
        redisTemplate.delete(bloomFilter.getName());
        String value="12345";
        boolean add= bloomFilter.add(value);
        Assert.assertFalse(add);
        boolean contains=bloomFilter.contains(value);
        Assert.assertTrue(contains);
    }


}
