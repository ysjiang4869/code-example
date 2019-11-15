package org.jys.example.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author jiangyuesong
 * @date 2018/12/6
 * @description
 * 仅当使用基于注解@kafkaListener时，才有必要使用@EnableKafka，@EnableKafka会做很多事情
 * 1. 首先，如果没有ConsumerFactory.class的bean，则创建，使用来自配置文件的配置
 * 2. 没有name为kafkaListenerContainerFactory的bean则创建它，这个是默认factory，注入ConsumerFactory<Object,Object>
 *     这时候，如果定义了ConsumerFactory.class的bean，但是没有定义kafkaListenerContainerFactory的bean，
 *     且ConsumerFactory不是<Object,Object>的类型，则会报错，不能注入
 *  3. 如果@afkaListener没有指定factory，使用的是默认的，如果不是手动定义默认的factory，则可能使用的配置和预期的完全不同！！
 *  所以，当使用@EnableKafka时，建议要么手动创建name为kafkaListenerContainerFactory的bean，要么ConsumerFactory不要定义成bean！
 */
@Configuration
@EnableKafka
public class SpringKafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;

    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private boolean autoCommit;

    @Value("${kafka.mode}")
    private String kafkaMode;

    @Value("${kafka.consumer.batch}")
    private boolean batch;

    private final GenericMessageListener listener;

    /**
     * 根据配置文件，自动提交和是否使用aware来确定注入的listener
     * @param listener message listener
     */
    @Autowired
    public SpringKafkaConsumerConfig(GenericMessageListener listener) {
        this.listener = listener;
    }

    @Bean
    @ConditionalOnProperty(value = "kafka.mode",havingValue = "spring-annotation-single")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String,String>> singleContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,String> factory=new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(null);//或者设置为1
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    @ConditionalOnProperty(value = "kafka.mode",havingValue = "spring-annotation-multi")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String,String>> concurrentContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,String> factory=new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(15);
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    /**
     * 基于接口的情况下的配置，单线程
     * @return listener 容器
     */
    @Bean
    @ConditionalOnProperty(value = "kafka.mode",havingValue = "spring-interface-single")
    public KafkaMessageListenerContainer<String,String> interfaceBasedSingleAdvancedKafkaContainer(){
        ContainerProperties properties=new ContainerProperties("test1","test2");
        //这里我采用了注入的形式,是为了可以根据条件创建listener
        //实际使用中，直接new对象就可以
        properties.setMessageListener(listener);
        properties.setGroupId("interface-single");
        if(!autoCommit){
            //注意：如果是配置了自动提交，提交是被spring接管的，更新信息可以看spring的AckMode,要配置真正的我们手动提交
            //需要增加此配置
            properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        }
        KafkaMessageListenerContainer<String,String> container= new KafkaMessageListenerContainer<>(consumerFactory(), properties);
        container.setAutoStartup(true);
        return container;
    }

    /**
     * 基于接口情况下的配置，多线程
     * @return listener 容器
     */
    @Bean
    @ConditionalOnProperty(value = "kafka.mode",havingValue = "spring-interface-multi")
    public ConcurrentMessageListenerContainer<String,String> interfaceBasedMultiAdvancedKafkaContainer(){
        ContainerProperties properties=new ContainerProperties("test1","test2");
        if(!autoCommit){
            properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        }
        properties.setGroupId("interface-multi");
        properties.setMessageListener(listener);
        ConcurrentMessageListenerContainer<String,String> container= new  ConcurrentMessageListenerContainer<>(consumerFactory(),properties);
        container.setConcurrency(15);
        container.setAutoStartup(true);
        return container;
    }

    public ConsumerFactory<String,String> consumerFactory(){
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    /**
     * kafka配置，更多参数参见ConsumerConfig.class
     * @return consumer config map
     */
    public Map<String,Object> consumerConfigs(){
        Map<String,Object> props=new HashMap<>(16);
        //kafka ip和端口，多个逗号分隔
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,servers);
        //kafka信息的key类型
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        //kafka信息的value类型
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class);
        //配置启动时offset如何确定，latest使用最新的，earliest总是从头开始消费
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");
        //自动提交offset时间间隔
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,500);
        //是否自动提交
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,autoCommit);
        //拉取数据最大bytes
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG,52428800);
        //两次poll直接最长间隔，超过该时间，server会认为客户端已经离线
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,5000);
        //poll的最大记录数量
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,1000);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG,100);

        //高级消费模式下，partition分配方式，参见我的博客https://jiangyuesong.me
        //注释掉该配置，查看高级多线程消费下partition的分配
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,  "org.apache.kafka.clients.consumer.RoundRobinAssignor");
        return props;
    }
}
