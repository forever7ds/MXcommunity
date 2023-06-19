package com.example.mxcommunity.controller;


import com.example.mxcommunity.Utils.*;
import com.example.mxcommunity.entity.Event;
import com.example.mxcommunity.entity.model.Comment;
import com.example.mxcommunity.event.EventProducer;
import com.example.mxcommunity.service.CommentService;
import com.example.mxcommunity.service.ThemePostService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private CommentService commentService;

    @Autowired
    private ThemePostService themePostService;

    @PostMapping("/add")
    @ResponseBody
    // 添加评论
    public String addComment(@RequestParam("content") String content,
                             @RequestParam("entityType") int entityType,
                             @RequestParam("entityId") long entityId,
                             @RequestParam("creatorId") long creatorId,
                             @RequestParam("targetId") long targetId){
        if(StringUtils.isBlank(content)){
            return CommunityUtil.getJSONString(1, "添加评论失败,内容不能为空!");
        }

        Comment addedComment = new Comment();
        addedComment.initDefaultField();
        addedComment.setContent(content);
        addedComment.setEntityType(entityType);
        addedComment.setEntityId(entityId);
        addedComment.setCreatorId(creatorId);
        addedComment.setTargetId(targetId);
        Map<String, Object> addCallback = commentService.addComment(addedComment);

        if(addCallback.containsKey("successfulMsg")){
            Event event = new Event();
            event.setTopic(ServiceConstants.TOPIC_COMMENT);
            event.setUserId(creatorId);
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setEntityUserId(targetId);
            eventProducer.publishEvent(event);
            if(addedComment.getEntityType() == CommentConstants.COMMENT_ENTITY_TYPE_POST){
                themePostService.updateModifiedTimeById(addedComment.getEntityId(), System.currentTimeMillis());
                redisTemplate.opsForSet().add(RedisUtil.getHeatPostsKey(), addedComment.getEntityId());

            }
            else if(addedComment.getEntityType() == CommentConstants.COMMENT_ENTITY_TYPE_COMMENT){
                themePostService.updateModifiedTimeById(commentService.getCommentById(addedComment.getEntityId()).getEntityId(), System.currentTimeMillis());
                redisTemplate.opsForSet().add(RedisUtil.getHeatPostsKey(), commentService.getCommentById(addedComment.getEntityId()).getEntityId());
            }
            return CommunityUtil.getJSONString(0);
        }
        return CommunityUtil.getJSONString(1, (String) addCallback.get("failedMsg"));
    }

    // 删除评论
    @PostMapping("/delete")
    @ResponseBody
    public String deleteComment(@RequestParam("id") long id){
        commentService.updateStatusById(id, CommentConstants.COMMENT_STATUS_BANNED);
        return CommunityUtil.getJSONString(0);
    }
    // 隐藏评论

    @PostMapping("/conceal")
    @ResponseBody
    public String concealComment(@RequestParam("id") long id){
        commentService.updateStatusById(id, CommentConstants.COMMENT_STATUS_INVISIBLE);
        return CommunityUtil.getJSONString(0);
    }

}
