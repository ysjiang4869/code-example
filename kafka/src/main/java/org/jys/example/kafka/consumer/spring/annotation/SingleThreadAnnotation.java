package org.jys.example.kafka.consumer.spring.annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author jiangyuesong
 * @date 2018/12/6
 * @description
 */
@Component
@ConditionalOnProperty(value = "kafka.mode",havingValue = "spring-annotation-single")
public class SingleThreadAnnotation {

    //逐条，批量，auto，noauto，aware,noaware
    @KafkaListener(topics = {"test1","test2"},containerFactory = "singleContainerFactory")
    public void singleBaseConsumer(){

    }

    @KafkaListener(topics = {"test1","test2"},containerFactory = "singleContainerFactory")
    public void singleBaseAwareConsumer(){

    }

    @KafkaListener(topics = {"test1","test2"},containerFactory = "singleContainerFactory")
    public void singleManualOffsetConsumer(){

    }

    @KafkaListener(topics = {"test1","test2"},containerFactory = "singleContainerFactory")
    public void singleManualOffsetAwareConsumer(){

    }

    @KafkaListener(topics = {"test1","test2"},containerFactory = "singleContainerFactory")
    public void batchBaseConsumer(){

    }

    @KafkaListener(topics = {"test1","test2"},containerFactory = "singleContainerFactory")
    public void batchBaseAwareConsumer(){

    }

    @KafkaListener(topics = {"test1","test2"},containerFactory = "singleContainerFactory")
    public void batchManualOffsetConsumer(){

    }

    @KafkaListener(topics = {"test1","test2"},containerFactory = "singleContainerFactory")
    public void batchManualOffsetAwareConsumer(){

    }
}
