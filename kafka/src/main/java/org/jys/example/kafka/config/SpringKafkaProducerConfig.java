package org.jys.example.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangyuesong
 * @date 2018/12/12
 */
@Configuration
public class SpringKafkaProducerConfig {


    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;

    @Bean
    public KafkaTemplate<String,String> kafkaTemplate(){
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String,String> producerFactory(){
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }
    @Bean
    public Map<String,Object> producerConfigs(){
        Map<String,Object> props=new HashMap<>(16);
        //kafka ip和端口，多个逗号分隔
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,servers);
        //kafka信息的key类型
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //kafka信息的value类型
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }
}
