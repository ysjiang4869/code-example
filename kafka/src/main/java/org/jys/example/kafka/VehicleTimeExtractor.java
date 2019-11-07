package org.jys.example.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author YueSong Jiang
 * @date 2019/9/23
 */
public class VehicleTimeExtractor implements TimestampExtractor {

    private static final String TIME_KEY="passTime";

    private final ObjectMapper mapper=new ObjectMapper();

    private final TypeReference<Map<String,Object>> mapType=new TypeReference<Map<String, Object>>() {};

    private static final Logger logger= LoggerFactory.getLogger(VehicleTimeExtractor.class);

    @Override
    public long extract(ConsumerRecord<Object, Object> consumerRecord, long l) {
        String json=consumerRecord.value().toString();
        try {
            Map<String,Object> data=mapper.readValue(json,mapType);
            if(data.containsKey(TIME_KEY)){
                return Long.parseLong(data.get(TIME_KEY).toString())*1000;
            }
        }catch (IOException e){
            logger.error(e.getMessage(), e);
        }
        return 0;
    }
}
