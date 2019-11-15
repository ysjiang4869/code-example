package org.jys.example.kafka.consumer.spring.interfaces.batch;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jys.example.kafka.utils.RecordLogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author YueSong Jiang
 */

@Component
@ConditionalOnExpression("${spring.kafka.consumer.enable-auto-commit}==false&&${kafka.listener.aware}==false&&${kafka.consumer.batch}==true")
public class BatchManualOffsetListener implements BatchAcknowledgingMessageListener<String,String> {

    private static final Logger logger= LoggerFactory.getLogger(BatchManualOffsetListener.class);
    @Override
    public void onMessage(List<ConsumerRecord<String, String>> data, Acknowledgment acknowledgment) {
        String logs= RecordLogUtils.getRecordLogs(data);
        logger.info(logs);
        logger.info("commit offset");
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }
        acknowledgment.acknowledge();
    }
}
