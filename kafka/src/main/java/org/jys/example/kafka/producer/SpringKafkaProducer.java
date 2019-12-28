package org.jys.example.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * @author jiangyuesong
 * @date 2018/12/12
 */
@Component
@Profile("kafka")
public class SpringKafkaProducer {

    private final KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    public SpringKafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 1000)
    public void sendData(){
        String message=Instant.now().toString();
        kafkaTemplate.send("test1",message);
        kafkaTemplate.send("test2",message);
    }
}
