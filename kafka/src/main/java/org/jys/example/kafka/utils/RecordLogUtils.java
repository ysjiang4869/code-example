package org.jys.example.kafka.utils;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiangyuesong
 * @date 2018/12/12
 * @description <p> </p>
 */
public class RecordLogUtils {

    public static String getRecordLogs(ConsumerRecord<String,String> consumerRecord){
        List<String> logs=new ArrayList<>();
        addRecordInfo(logs,consumerRecord);
        logs.add(String.format("current thread: %s",Thread.currentThread().getId()));
        return String.join("\n",logs);
    }

    public static String getRecordLogs(ConsumerRecord<String,String> consumerRecord, Consumer<?, ?> consumer){
        List<String> logs=new ArrayList<>();
        logs.add(getRecordLogs(consumerRecord));
        addConsumerInfo(logs,consumer);
        return String.join("\n",logs);
    }

    public static String getRecordLogs(List<ConsumerRecord<String, String>> consumerRecords){
        List<String> logs=new ArrayList<>();
        logs.add("receive batch size: "+consumerRecords.size());
        for (ConsumerRecord<String,String> consumerRecord:consumerRecords) {
            addRecordInfo(logs,consumerRecord);
        }
        logs.add(String.format("current thread: %s",Thread.currentThread().getId()));
        return String.join("\n",logs);
    }

    public static String getRecordLogs(List<ConsumerRecord<String, String>> consumerRecords, Consumer<?, ?> consumer){
        List<String> logs=new ArrayList<>();
        logs.add(getRecordLogs(consumerRecords));
        addConsumerInfo(logs,consumer);
        return String.join("\n",logs);
    }

    private static void addConsumerInfo(List<String> logs,Consumer<?,?> consumer){
        String assignments=consumer.assignment().stream().map(TopicPartition::toString).collect(Collectors.joining(","));
        logs.add(String.format("consumer assign partitions: %s",assignments));
        logs.add(String.format("consumer hash: %s",consumer.hashCode()));
    }

    private static void addRecordInfo(List<String> logs,ConsumerRecord<String,String> consumerRecord){
        logs.add(String.format("receive record with message:%s",consumerRecord.value()));
        logs.add(String.format("record info: \n topic %s\n partition %s \noffset %s\n",consumerRecord.topic(),
                consumerRecord.partition(),consumerRecord.offset()));
    }
}
