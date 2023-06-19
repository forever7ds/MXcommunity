package com.example.mxcommunity.event;


import com.alibaba.fastjson.JSONObject;
import com.example.mxcommunity.Utils.CommunityUtil;
import com.example.mxcommunity.Utils.ServiceConstants;
import com.example.mxcommunity.entity.Event;
import com.example.mxcommunity.entity.model.Message;
import com.example.mxcommunity.entity.model.ThemePost;
import com.example.mxcommunity.service.MessageService;
import com.example.mxcommunity.service.SearchService;
import com.example.mxcommunity.service.ThemePostService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private ThemePostService themePostService;

    @Autowired
    private SearchService searchService;

    @KafkaListener(topics = {ServiceConstants.TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空");
            return ;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return ;
        }

        ThemePost post = themePostService.getPostDetailById(event.getEntityId());
        System.out.println("Consume time is : " + System.nanoTime());
        searchService.saveThemePost(post);

    }

    @KafkaListener(topics = {ServiceConstants.TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空");
            return ;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return ;
        }
        searchService.deleteThemePost(event.getEntityId());

    }

    @KafkaListener(topics = {ServiceConstants.TOPIC_COMMENT, ServiceConstants.TOPIC_LIKE})
    public void handleLikeAndCommentMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空");
            return ;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误");
            return ;
        }

        // 创建并发送系统Message
        Message message = new Message();
        message.initDefaultField();
        message.setFromId(ServiceConstants.SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);

    }
}
