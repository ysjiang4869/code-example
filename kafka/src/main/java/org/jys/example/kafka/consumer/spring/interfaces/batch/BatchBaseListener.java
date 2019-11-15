package org.jys.example.kafka.consumer.spring.interfaces.batch;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jys.example.kafka.utils.RecordLogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author jiangyuesong
 * @date 2018/12/6
 * 批量消费
 * 注意：每次consumer poll都是poll一批数据的
 * spring配置batch与否，只是决定了消费时是一条一条处理还是批量处理
 */
@Component
@ConditionalOnExpression("${spring.kafka.consumer.enable-auto-commit}==true&&${kafka.listener.aware}==false&&${kafka.consumer.batch}==true")
public class BatchBaseListener implements BatchMessageListener<String,String> {

    private static final Logger logger= LoggerFactory.getLogger(BatchBaseListener.class);

    @Override
    public void onMessage(List<ConsumerRecord<String, String>> data) {
        String logs= RecordLogUtils.getRecordLogs(data);
        logger.info(logs);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }
    }
}
