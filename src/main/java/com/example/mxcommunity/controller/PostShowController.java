package com.example.mxcommunity.controller;

import com.example.mxcommunity.Utils.*;
import com.example.mxcommunity.entity.Event;
import com.example.mxcommunity.entity.Page;
import com.example.mxcommunity.entity.model.Comment;
import com.example.mxcommunity.entity.model.ThemePost;
import com.example.mxcommunity.event.EventProducer;
import com.example.mxcommunity.service.CommentService;
import com.example.mxcommunity.service.ThemePostService;
import com.example.mxcommunity.service.UserService;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.context.Theme;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Controller
public class PostShowController {

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    ThemePostService themePostService;

    @Autowired
    CommentService commentService;

    @Autowired
    UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    EventProducer eventProducer;



    @GetMapping("/publish")
    public String goToEditPost(){return "/site/edit_post";}
    @PostMapping("/publish")
    // 发帖(位于帖子编辑页面)
    public String publishPost(@RequestParam("title") String title,
                              @RequestParam("content") String content,
                              @RequestParam("creator_id") long creatorId,
                              @RequestParam("type") int type,
                              @RequestParam("tag") String tag,
                              Model model){

        // 聚合生成ThemePost对象

        // 异常判断
        if(StringUtils.isBlank(title)){
            model.addAttribute("failedMsg", "标题为空");
            return "/site/failed_page";
        }

        // 初始化新的ThemePost对象
        ThemePost publishToPost = new ThemePost();
        publishToPost.initDefaultField();
        publishToPost.setTitle(title);
        publishToPost.setContent(content);
        publishToPost.setCreatorId(creatorId);
        publishToPost.setType(type);
        publishToPost.setTag(tag);
        publishToPost.setHeatScore(0.9);
        Map<String, Object> publishPostCallback = themePostService.publishNewPost(publishToPost);

        if(publishPostCallback.containsKey("successfulMsg")){
            model.addAttribute("successfulMsg", publishPostCallback.get("successfulMsg"));

            Event event = new Event();
            event.setTopic(ServiceConstants.TOPIC_PUBLISH);
            event.setUserId(creatorId);
            event.setEntityType(CommentConstants.COMMENT_ENTITY_TYPE_POST);
            event.setEntityId(publishToPost.getId());
            event.setEntityUserId(publishToPost.getCreatorId());
            eventProducer.publishEvent(event);


            redisTemplate.opsForSet().add(RedisUtil.getHeatPostsKey(), publishToPost.getId());
            // 成功
            return "/site/successful_page";
        }

        model.addAttribute("failedMsg", publishPostCallback.get("failedMsg"));
        // 失败
        return "/site/failed_page";
    }

    long startTime = System.nanoTime();
    // 获取实时热榜帖列表
    @PostMapping("/index")
    public String getThemePosts(Model model,
                                @RequestBody Page page,
                                @RequestParam int mode,
                                @RequestParam int type){

        page.setItemCount(themePostService.getCountPosts());
        page.setPath("/index?orderMode=" + mode + "&type=" + type);
        if(hostHolder.getUser() != null){
            System.out.println("index page used by user : " + hostHolder.getUser().getUsername());
        }
        else{
            System.out.println("no user index !");
        }


        // 获取热榜(Cache)
        List<ThemePost> orderedPosts;
        long startTime = System.nanoTime();
        if(type == -1) {
            orderedPosts = themePostService.getPagingPosts(page, mode);
        }
        else {
            orderedPosts = themePostService.getPagingPostsByType(page, mode, type);
        }
        System.out.println("caffeine cost time is " + (System.nanoTime() - startTime) + " ns");

        List<Map<String, Object>> postMaps = new ArrayList<>();
        if(orderedPosts != null) System.out.println("Get ordered posts successfully ! " + orderedPosts);
        else System.out.println("Get ordered posts failed ! " + orderedPosts);
        if(orderedPosts != null){
            for(ThemePost post : orderedPosts){
                Map<String, Object> postMap = new HashMap<>();
                postMap.put("post", post);
                postMap.put("user", userService.getNameById(post.getCreatorId()));
                postMaps.add(postMap);
            }
        }
        // 返回值赋值
        model.addAttribute("themePosts", postMaps);
        model.addAttribute("mode", mode);
        model.addAttribute("type", -1);
        System.out.println("caffeine full : " + (System.nanoTime() - startTime));
        return "/site/index";
    }

    // 根据关键词获取符合的帖列表
    @PostMapping("/search")
    public String getSearchPosts(@RequestParam("keywords") String keywords,
                                 Model model,
                                 Page page){
        // 获取关键词搜索结果
        List<Map<String, Object>> retPosts = new ArrayList<>();
        // 结果赋值
        model.addAttribute("searchPosts", retPosts);
        model.addAttribute("keywords", keywords);
        return "/site/search_result";
    }


    // 查看某个帖(跳转)
    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable("id") long postId,
                             Model model,
                             Page page){
        // 获取post
        ThemePost post = themePostService.getPostDetailById(postId);
        model.addAttribute("user", userService.getNameById(post.getCreatorId()));
        model.addAttribute("postInfo", post);


        // 获取post对应的回复并分页
        page.setLimit(5);
        page.setPath("/posts/detail/" + postId);
        page.setItemCount(post.getCommentCount());
        List<Comment> comments = commentService.getPagingCommentsByPostId(postId, page);
        List<Map<String, Object>> commentMaps = new ArrayList<>();
        // 将每个回复设置为一个Map实例
        if(comments != null){
            for(Comment comment : comments){
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("comment", comment);
                commentMap.put("creator", userService.getNameById(comment.getCreatorId()));

                // 将每个回复的子回复不分页地检索,每个子回复绑定为Map,再聚合成List
                List<Comment> subComments = commentService.getCommentsByCommentId(comment.getId());
                List<Map<String, Object>> subCommentMaps = new ArrayList<>();
                if(subComments != null){
                    for(Comment subComment : subComments) {
                        HashMap<String, Object> subCommentMap = new HashMap<>();
                        subCommentMap.put("reply", subComment);
                        subCommentMap.put("from", userService.getNameById(subComment.getCreatorId()));
                        subCommentMap.put("to", userService.getNameById(subComment.getTargetId()));
                        subCommentMaps.add(subCommentMap);
                    }
                }
                commentMap.put("subComments", subCommentMaps);
                commentMap.put("subCommentsCount", subCommentMaps.size());
                commentMaps.add(commentMap);
            }
        }
        model.addAttribute("postComments", commentMaps);

        // 增加访问量
        String accessKey = RedisUtil.getAccessKey();
        String accessHashKey = RedisUtil.getAccessHashKey(postId);
        redisTemplate.opsForHash().increment(accessKey, accessHashKey, 1);
        return "/site/detail_page";
    }

    // 设置为置顶/加精/删除状态
    @PostMapping("/updateType")
    @ResponseBody
    public String updatePostStatus(@RequestParam("id") long id,
                                   @RequestParam("handler_id") long handlerId,
                                   @RequestParam("post_creator_id") long postCreatorId,
                                   @RequestParam("type") int type){
        boolean callback = themePostService.updateTypeById(id, type);
        if(callback){
            // 消息队列设置
            if(type == ThemePostConstants.POST_TYPE_BANNED) {
                Event event = new Event();
                event.setTopic(ServiceConstants.TOPIC_DELETE);
                event.setUserId(handlerId);
                event.setEntityType(CommentConstants.COMMENT_ENTITY_TYPE_POST);
                event.setEntityId(id);
                event.setEntityUserId(postCreatorId);
                eventProducer.publishEvent(event);
            }
            else if(type == ThemePostConstants.POST_TYPE_ESSENTIAL || type == ThemePostConstants.POST_TYPE_EVENT){
                Event event = new Event();
                event.setTopic(ServiceConstants.TOPIC_PUBLISH);
                event.setUserId(handlerId);
                event.setEntityType(CommentConstants.COMMENT_ENTITY_TYPE_POST);
                event.setEntityId(id);
                event.setEntityUserId(postCreatorId);
                // for(int i=0;i<500;i++) {
                eventProducer.publishEvent(event);
                // }
            }
            // redis相关设置
            if(type == ThemePostConstants.POST_TYPE_ESSENTIAL){
                redisTemplate.opsForSet().add(RedisUtil.getHeatPostsKey(), id);
            }
            return CommunityUtil.getJSONString(0);
        }
        return CommunityUtil.getJSONString(1);
    }

    /**
     * 进入 500 错误界面
     * @return
     */
    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    /**
     * 没有权限访问时的错误界面（也是 404）
     * @return
     */
    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }


}
