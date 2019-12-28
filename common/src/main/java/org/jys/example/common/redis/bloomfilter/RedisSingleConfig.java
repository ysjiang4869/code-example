package org.jys.example.common.redis.bloomfilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * @author YueSong Jiang
 * @date 2019/3/15
 * Redis single configuration based on lettuce
 */
@Configuration
@ConditionalOnProperty(name = "redis.wifi.cluster", havingValue = "false", matchIfMissing = true)
public class RedisSingleConfig {

    @Value("${redis.wifi.nodes}")
    private String[] hosts;

    @Value("${redis.wifi.password}")
    private String password;

    private static final Logger logger = LoggerFactory.getLogger(RedisSingleConfig.class);

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        if (hosts.length == 0) {
            throw new IllegalArgumentException("no redis host supplied");
        }
        if (hosts.length > 1) {
            throw new IllegalArgumentException("except one redis host but get more than one");
        }

        String[] url = hosts[0].split(":");
        if (url.length != 2) {
            logger.error("wrong redis host format, need ip:port format, byt get [{}]", hosts[0]);
            throw new IllegalArgumentException("wrong redis host format");
        }
        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration(url[0], Integer.parseInt(url[1]));
        cfg.setPassword(password);
        return new LettuceConnectionFactory(cfg);
    }

    @Bean(name = "bloomFilterRedisTemplate")
    public StringRedisTemplate redisTemplate() {
        return new StringRedisTemplate(redisConnectionFactory());
    }
}
