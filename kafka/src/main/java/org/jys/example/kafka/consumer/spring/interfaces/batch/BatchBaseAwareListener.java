package org.jys.example.kafka.consumer.spring.interfaces.batch;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import org.jys.example.kafka.utils.RecordLogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.listener.BatchConsumerAwareMessageListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author YueSong Jiang
 */

@Component
@ConditionalOnExpression("${spring.kafka.consumer.enable-auto-commit}==true&&${kafka.listener.aware}==true&&${kafka.consumer.batch}==true")
public class BatchBaseAwareListener implements BatchConsumerAwareMessageListener<String, String> {

    private static final Logger logger = LoggerFactory.getLogger(BatchBaseAwareListener.class);

    @Override
    public void onMessage(List<ConsumerRecord<String, String>> data, Consumer<?, ?> consumer) {
        String logs = RecordLogUtils.getRecordLogs(data, consumer);
        logger.info(logs);
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
