package org.jys.example.kafka.consumer.spring.interfaces.single;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jys.example.kafka.utils.RecordLogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

/**
 * @author jiangyuesong
 * @date 2018/12/6
 */
@Component
@ConditionalOnExpression("${spring.kafka.consumer.enable-auto-commit}==true&&${kafka.listener.aware}==false&&${kafka.consumer.batch}==false")
public class SingleBaseListener implements MessageListener<String,String> {

    private static final Logger logger= LoggerFactory.getLogger(SingleBaseListener.class);
    @Override
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
       String logs= RecordLogUtils.getRecordLogs(consumerRecord);
       logger.info(logs);
    }
}
