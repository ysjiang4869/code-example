package org.jys.example.flink;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.operators.StreamingRuntimeContext;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.PurgingTrigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.runtime.tasks.ProcessingTimeCallback;
import org.apache.flink.streaming.runtime.tasks.ProcessingTimeService;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.util.Collector;

import org.jys.example.flink.utils.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * @author YueSong Jiang
 * @date 2019/9/25
 */
@Component
@Profile("flink")
public class FlinkStreamWindowedCount implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient final StreamExecutionEnvironment env;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServers;

    @Value("${stream.kafka.data_topic}")
    private String dataTopic;



    @Autowired
    public FlinkStreamWindowedCount(StringRedisTemplate redisTemplate) throws IOException {
//        env = StreamExecutionEnvironment.
//                createRemoteEnvironment("192.168.14.36",52971,"C:\\jys\\code\\stream-example\\target\\stream-example-1.0-shaded.jar");
        env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
//        env.getConfig().setAutoWatermarkInterval();
        env.enableCheckpointing(600000);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.setStateBackend(new RocksDBStateBackend("file:///flink/state/"));
    }

    @PostConstruct
    public void startStream() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", kafkaServers);
        properties.setProperty("group.id", "vehicle-count");
        FlinkKafkaConsumer<ObjectNode> consumer=new FlinkKafkaConsumer<ObjectNode>(dataTopic, new JSONKeyValueDeserializationSchema(true), properties);
        consumer.assignTimestampsAndWatermarks(new KafkaTimestampExtractor(Time.minutes(5)));
        consumer.setStartFromEarliest();
        DataStream<ObjectNode> stream = env.addSource(consumer);
        stream
                .filter(k->k.get("value").has("tollgateID"))
                .keyBy(map -> map.get("value").get("tollgateID").asText())
                .timeWindow(Time.seconds(15))
                .allowedLateness(Time.minutes(2))
                .trigger(PurgingTrigger.of(ContinuousProcessingTimeTrigger.of(Time.seconds(30))))
                .process(new ProcessWindowFunction<ObjectNode, Tuple3<TimeWindow,String,Long>, String, TimeWindow>() {
                    private ValueStateDescriptor<Long> countState =new ValueStateDescriptor<Long>("cont-num",Long.class);
                    @Override
                    public void process(String s, Context context, Iterable<ObjectNode> elements, Collector<Tuple3<TimeWindow,String,Long>> out) throws Exception {
                        //called every time windowed calculate was triggered
                        long count=0;
                        for (ObjectNode x :elements) {
                            count++;
                        }
                        //use cache is success, but if pure every time,
                        // when there is no data, it will not call process any more
                        Long origin=context.windowState().getState(countState).value();
                        if(origin==null){
                            origin=0L;
                            System.out.println("init state");
                        }else {
                            System.out.println(context.window().toString()+":"+origin);
                        }
                        count+=origin;
                        System.out.println(count);
                        context.windowState().getState(countState).update(count);
                        out.collect(new Tuple3<>(context.window(),s,count));
                    }
                })
                .addSink(new RedisCountSink());
        Executor executor= Executors.newFixedThreadPool(1);

        CompletableFuture<JobExecutionResult> future=CompletableFuture.supplyAsync(new Supplier<JobExecutionResult>() {
            @Override
            public JobExecutionResult get() {
                try {
                    return env.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }, executor);
    }

    private static class KafkaTimestampExtractor extends BoundedOutOfOrdernessTimestampExtractor<ObjectNode> {

        private static final String TIME_KEY="passTime";


        public KafkaTimestampExtractor(Time maxOutOfOrderness) {
            super(maxOutOfOrderness);
        }

        @Override
        public long extractTimestamp(ObjectNode element) {
            return element.get("value").get(TIME_KEY).asLong(0)*1000;
        }
    }



    private static class RedisCountSink extends RichSinkFunction<Tuple3<TimeWindow,String,Long>> implements ProcessingTimeCallback {


        private ConcurrentLinkedQueue<Tuple3<TimeWindow,String,Long>> cache;
        private transient StringRedisTemplate template;

        private final String sortedSetKey = "vehicle:stream:vehicle-count";

        private long duration=30000;

        private ProcessingTimeService processingTimeService;

        @Override
        public void open(Configuration parameters) throws Exception {
            super.open(parameters);
            template = SpringUtils.getBean(StringRedisTemplate.class);
            cache=new ConcurrentLinkedQueue<>();
            this.processingTimeService = ((StreamingRuntimeContext) getRuntimeContext()).getProcessingTimeService();
            long currentProcessingTime = processingTimeService.getCurrentProcessingTime();
            processingTimeService.registerTimer(currentProcessingTime + duration, this);
        }

        @Override
        public void invoke(Tuple3<TimeWindow,String,Long> value, Context context) throws Exception {
            cache.add(value);
            if(cache.size()>10000){
                flush();
            }
        }
        private void flush() {
            template.executePipelined((RedisCallback<Object>) redisConnection -> {
                StringRedisConnection stringCoon = (StringRedisConnection) redisConnection;
                while (!cache.isEmpty()) {
                    Tuple3<TimeWindow,String,Long> data = cache.poll();//
                    TimeWindow k = data.f0;
                    long time = k.getStart();
                    System.out.println("consume "+data.f1);
                    String key = sortedSetKey + ":" + time;
                    stringCoon.zAdd(sortedSetKey, time, key);
                    stringCoon.hSet(key, data.f1, data.f2.toString());
                }
                return null;
            });
        }

        @Override
        public void onProcessingTime(long timestamp) throws Exception {
            final long currentTime = processingTimeService.getCurrentProcessingTime();
            flush();
            processingTimeService.registerTimer(currentTime + duration, this);
        }
    }
}
