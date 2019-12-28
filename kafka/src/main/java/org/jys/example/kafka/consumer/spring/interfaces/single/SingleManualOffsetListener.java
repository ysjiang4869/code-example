package org.jys.example.kafka.consumer.spring.interfaces.single;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jys.example.kafka.utils.RecordLogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * @author jiangyuesong
 * @date 2018/12/12
 * 手动提交offset
 * 注意：如果是自动提交，是由kafka-clients定时提交的
 * 当kafka配置为手动提交时（auto-commit=false),kafka-clients不再自动提交
 * offset提交被spring接管
 * 只有配置合适的ack mode,才能真正的实现手动提交
*/
@Component
@ConditionalOnExpression("${spring.kafka.consumer.enable-auto-commit}==false&&${kafka.listener.aware}==false&&${kafka.consumer.batch}==false")
public class SingleManualOffsetListener implements AcknowledgingMessageListener<String,String> {

    private static final Logger logger= LoggerFactory.getLogger(SingleManualOffsetListener.class);
    @Override
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        String logs= RecordLogUtils.getRecordLogs(consumerRecord);
        logger.info(logs);
        logger.info("commit offset");
        acknowledgment.acknowledge();
    }
}
