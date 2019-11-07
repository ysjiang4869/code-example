package org.jys.example.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YueSong Jiang
 * @date 2019/9/23
 */
@Configuration
public class KafkaConfiguration {


    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;

    @Bean
    public KafkaTemplate<String,String> producer(){
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaAdmin kafkaAdmin(){
        Map<String, Object> configs = new HashMap<>(1);
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        KafkaAdmin admin=new KafkaAdmin(configs);
        admin.setFatalIfBrokerNotAvailable(true);
        return admin;
    }

    @Bean
    public ProducerFactory<String,String> producerFactory(){
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public Map<String,Object> producerConfigs(){
        Map<String,Object> properties = new HashMap<>(10);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,servers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class);
        return properties;
    }
}
