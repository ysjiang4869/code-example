package org.jys.example.kafka.stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.*;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author YueSong Jiang
 * @date 2019/9/23
 */
@Component
@Profile("stream")
public class KafkaStreamWindowedCount {


    private String dataTopic;

    private Properties properties;

    private final ObjectMapper mapper = new ObjectMapper();

    private final TypeReference<Map<String, Object>> mapType = new TypeReference<Map<String, Object>>() {
    };

    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamWindowedCount.class);


    private KafkaStreams stream;


    private final StringRedisTemplate redisTemplate;

    private final String sortedSetKey = "vehicle:stream:vehicle-count";


    @Autowired
    public KafkaStreamWindowedCount(@Value("${spring.kafka.bootstrap-servers}") String servers,
                                    StringRedisTemplate redisTemplate,
                                    @Value("${stream.kafka.data_topic}") String dataTopic,
                                    @Value("${stream.kafka.stream_thread}") int streamThread,
                                    @Value("${stream.kafka.replicas_num}")int replicas) {
        properties = new Properties();
        this.dataTopic = dataTopic;
        properties.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, StreamTimeExtractor.class);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, dataTopic);
        properties.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, replicas);
        properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, streamThread);
        properties.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        properties.put(StreamsConfig.STATE_DIR_CONFIG, "kafka/state/");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void startStream() {
        StreamsBuilder builder = new StreamsBuilder();
        KTable<Windowed<String>, Long> table =
                builder.stream(dataTopic, Consumed.with(Serdes.String(), Serdes.String()))
                        .map((key, value) -> {
                            try {
                                Map<String, Object> data = mapper.readValue(value, mapType);
                                if (data.containsKey("tollgateID")) {
                                    return KeyValue.pair(data.get("tollgateID").toString(), value);
                                } else {
                                    System.out.println("wrong data");
                                    return KeyValue.pair(null, value);
                                }

                            } catch (IOException e) {
                                logger.error(e.getMessage(), e);
                                return KeyValue.pair(null, value);
                            }
                        })
                        .filter((k, v) -> !v.isEmpty())
                        .groupBy((k, v) -> k)
                        .windowedBy(TimeWindows.of(Duration.ofSeconds(300)))
                        .count(Materialized.<String,Long, WindowStore<Bytes, byte[]>>as(dataTopic + "Store").withRetention(Duration.ofDays(1)));
        // store windowed data in redis
        // sorted set key is vehicle:stream:appName, value is hash Key, score is time
        // hash key is set-key:time, h-key is group param, h-value is count

        //this supress time depend count result update frequency
        table./*suppress(Suppressed.untilTimeLimit(Duration.ofSeconds(30), Suppressed.BufferConfig.maxRecords(10000))).*/
                toStream().process(new CountStore());
        stream = new KafkaStreams(builder.build(), properties);
        logger.info("begin kafka stream process");
        stream.start();
    }

    public KafkaStreams getStream() {
        return stream;
    }

    private class CountStore implements ProcessorSupplier<Windowed<String>, Long> {


        @Override
        public Processor<Windowed<String>, Long> get() {
            return new CountRedisStoreProcessor();
        }
    }

    private class CountRedisStoreProcessor extends AbstractProcessor<Windowed<String>, Long> {

        ConcurrentLinkedQueue<KeyValue<Windowed<String>, Long>> cache;

        @Override
        public void init(ProcessorContext context) {
            super.init(context);
            cache = new ConcurrentLinkedQueue<>();
            //this time depend count result update frequency
            //called process() every element arrive
            //called schedule() defined function when timer trigger
            this.context().schedule(Duration.ofSeconds(10), PunctuationType.WALL_CLOCK_TIME, (timestamp) -> flush());
        }

        @Override
        public void process(Windowed<String> key, Long value) {

            cache.add(KeyValue.pair(key, value));
        }

        private void flush() {
            redisTemplate.executePipelined((RedisCallback<Object>) redisConnection -> {
                StringRedisConnection stringCoon = (StringRedisConnection) redisConnection;
                long count = 0;
                // do not more than 10000 record one time
                while (!cache.isEmpty() && count < 50000) {
                    KeyValue<Windowed<String>, Long> data = cache.poll();//
                    Windowed<String> k = data.key;
                    long time = k.window().start();
                    String key = sortedSetKey + ":" + time;
                    stringCoon.zAdd(sortedSetKey, time, key);
                    stringCoon.hSet(key, k.key(), data.value.toString());
                    count++;
                }
                return null;
            });

            // commit the current processing progress
            context().commit();
        }

    }
}
