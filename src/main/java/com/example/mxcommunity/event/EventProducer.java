package com.example.mxcommunity.event;

import com.alibaba.fastjson.JSONObject;
import com.example.mxcommunity.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void publishEvent(Event event){
        System.out.println("Kafka event produce successfully !");
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
        System.out.println("Produce time is : " + System.nanoTime());
    }



}
