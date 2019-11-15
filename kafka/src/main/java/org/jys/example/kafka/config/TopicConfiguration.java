package org.jys.example.kafka.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangyuesong
 * @date 2018/12/6
 * kafka admin, check and create topic when application start
 */
@Configuration
public class TopicConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;

    @Bean
    public KafkaAdmin admin(){
        Map<String,Object> configs =new HashMap<>(1);
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        return new KafkaAdmin(configs);
    }

    /**
     * create topic test 1 with 5 partitions
     * @return new topic
     */
    @Bean
    public NewTopic topicTest1(){
        return new NewTopic("test1",5,(short)1);
    }

    /**
     * create topic test2 with 10 partitions
     * @return new topic
     */
    @Bean
    public NewTopic topicTest2(){
        return new NewTopic("test2",10,(short)1);
    }
}
