package com.example.mxcommunity.controller;

import com.example.mxcommunity.Utils.*;
import com.example.mxcommunity.entity.Event;
import com.example.mxcommunity.event.EventProducer;
import com.example.mxcommunity.service.LikeService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class LikeController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping("/like")
    @ResponseBody
    public String addLike(@RequestParam("creator_id")long creatorId,
                          @RequestParam("entity_type")int entityType,
                          @RequestParam("entity_id")long entityId,
                          @RequestParam("entity_user_id")long entityUserId,
                          @RequestParam("post_id")long postId){

        likeService.addLike(creatorId, entityType, entityId, entityUserId, postId);
        boolean status = likeService.getUserLikedEntityOrNot(creatorId, entityType, entityId);
        if(status){
            // 通知事件
            Event event = new Event();
            event.setTopic(ServiceConstants.TOPIC_LIKE);
            event.setUserId(creatorId);
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setEntityUserId(entityUserId);
            eventProducer.publishEvent(event);
            // post热度增加
            String heatRedisKey = RedisUtil.getHeatPostsKey();
            redisTemplate.opsForSet().add(heatRedisKey, postId);
            return CommunityUtil.getJSONString(0);
        }

        return CommunityUtil.getJSONString(1);


    }

}
