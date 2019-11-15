package org.jys.example.kafka.consumer.spring.interfaces.single;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jys.example.kafka.utils.RecordLogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * @author jiangyuesong
 * @date 2018/12/12
*/
@Component
@ConditionalOnExpression("${spring.kafka.consumer.enable-auto-commit}==false&&${kafka.listener.aware}==true&&${kafka.consumer.batch}==false")
public class SingleManualOffsetAwareListener implements AcknowledgingConsumerAwareMessageListener<String,String> {

    private static final Logger logger= LoggerFactory.getLogger(SingleManualOffsetAwareListener.class);

    @Override
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {

        String logs= RecordLogUtils.getRecordLogs(consumerRecord,consumer);
        logger.info(logs);
        logger.info("commit offset");
        acknowledgment.acknowledge();
    }
}
