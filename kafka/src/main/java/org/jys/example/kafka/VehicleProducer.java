package org.jys.example.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.jys.example.kafka.common.VehicleRandom;
import org.jys.example.kafka.entity.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author YueSong Jiang
 * @date 2019/9/23
 */
@Component
public class VehicleProducer {

    private final KafkaAdmin kafkaAdmin;

    private final KafkaTemplate<String,String> kafkaTemplate;

    private ObjectMapper mapper;

    @Value("${stream.kafka.data_topic}")
    private String dataTopic;

    @Value("${stream.kafka.data_count}")
    private int dataCount;

    @Value("${stream.kafka.continue_if_exist:false}")
    private boolean continueIfExist;

    private static final Logger logger= LoggerFactory.getLogger(VehicleProducer.class);

    public VehicleProducer(KafkaAdmin kafkaAdmin, KafkaTemplate<String, String> kafkaTemplate) {
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
                try {
                    for (int j = 0; j < dataCount/thread; j++) {
                        Vehicle vehicle= VehicleRandom.getRandomVehicle();
                        kafkaTemplate.send(dataTopic,mapper.writeValueAsString(vehicle))
                                .addCallback(stringStringSendResult -> {

                                }, throwable -> logger.error(throwable.getMessage(),throwable));
                    }
                } catch (JsonProcessingException e) {
                    logger.error(e.getMessage(),e);
                }
            }, executor);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]))
                .thenRunAsync(()-> logger.info("send 1 billion data to kafka success"));
    }

}
