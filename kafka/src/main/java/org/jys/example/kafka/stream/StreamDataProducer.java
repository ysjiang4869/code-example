package org.jys.example.kafka.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author YueSong Jiang
 * @date 2019/9/23
 */
@Component
@Profile("stream")
public class StreamDataProducer {

    private final KafkaAdmin kafkaAdmin;

    private final KafkaTemplate<String,String> kafkaTemplate;

    private ObjectMapper mapper;

    @Value("${stream.kafka.data_topic}")
    private String dataTopic;

    @Value("${stream.kafka.data_count}")
    private int dataCount;

    @Value("${stream.kafka.continue_if_exist:false}")
    private boolean continueIfExist;

    private static final Logger logger= LoggerFactory.getLogger(StreamDataProducer.class);

    @Autowired
    public StreamDataProducer(KafkaAdmin kafkaAdmin, KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaAdmin = kafkaAdmin;
        this.kafkaTemplate = kafkaTemplate;
        mapper=new ObjectMapper();
    }

    @PostConstruct
    public void prepareTopics() throws ExecutionException, InterruptedException {
        AdminClient client=AdminClient.create(kafkaAdmin.getConfig());
        Set<String> topics=client.listTopics().names().get();
        if(topics.contains(dataTopic)){
            // topic already exist
            if(!continueIfExist){
                logger.warn("topic already exists, will do nothing");
                return;
            }
        }
        //create the new topic
        NewTopic topic=new NewTopic(dataTopic,60,(short)0);
        client.createTopics(Collections.singletonList(topic));
        prepareTestData();
    }


    /**
     * prepare about 1 billion vehicle data in kafka
     */
    private void prepareTestData(){
        int thread=10;
        logger.info("begin generate data");
        Executor executor= Executors.newFixedThreadPool(thread);
        List<CompletableFuture<Void>> futureList=new ArrayList<>(thread);
        for (int i = 0; i < thread; i++) {
            CompletableFuture<Void> future=CompletableFuture.runAsync(() -> {
                Random random=new Random();
                for (int j = 0; j < dataCount/thread; j++) {
                    ObjectNode node=mapper.createObjectNode();
                    node.put("passtime", Instant.now().getEpochSecond());
                    node.put("tollgateID",Integer.toString(random.nextInt(1_0)));
                    node.put("recordid",UUID.randomUUID().toString());
                    kafkaTemplate.send(dataTopic,node.asText())
                            .addCallback(stringStringSendResult -> {

                            }, throwable -> logger.error(throwable.getMessage(),throwable));
                }
            }, executor);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenRunAsync(()-> logger.info("send 1 billion data to kafka success"));
    }

}
