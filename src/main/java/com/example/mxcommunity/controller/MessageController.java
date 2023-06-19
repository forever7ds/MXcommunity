package com.example.mxcommunity.controller;


import com.alibaba.fastjson.JSONObject;
import com.example.mxcommunity.Utils.CommunityUtil;
import com.example.mxcommunity.Utils.HostHolder;
import com.example.mxcommunity.Utils.ServiceConstants;
import com.example.mxcommunity.entity.Page;
import com.example.mxcommunity.entity.model.Message;
import com.example.mxcommunity.entity.model.User;
import com.example.mxcommunity.service.MessageService;
import com.example.mxcommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController {

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    /**
     * 私信列表
     * @param model
     * @param page
     * @return
     */
    @PostMapping("/letter/list")
    public String getLetterList(@RequestParam("user_id")long userId,
                                @RequestBody Page page,
                                Model model) {
        // Integer.valueOf("abc"); // 测试统一异常处理（普通请求）

        // 获取当前登录用户信息
        // User user = hostHolder.getUser();
        User user = userService.getUserById(userId);
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setItemCount(messageService.findConversationCount(user.getId()));
        // 私信列表
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getSQLStart(), page.getLimit());

        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message); // 私信
                map.put("letterCount", messageService.findLetterCount(
                        message.getConversationId())); // 私信数量
                map.put("unreadCount", messageService.findLetterUnreadCount(
                        user.getId(), message.getConversationId())); // 未读私信数量
                long targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.getUserById(targetId)); // 私信对方
                conversations.add(map);
            }
        }
        System.out.println("successfully get message : " + conversations.size());
        model.addAttribute("conversations", conversations);

        // 查询当前用户的所有未读消息数量
        long letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        long noticeUnreadCount = messageService.findNoticeUnReadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";

    }

    /**
     * 私信详情页
     * @param conversationId
     * @param page
     * @param model
     * @return
     */
    @PostMapping("/letter/detail")
    public String getLetterDetail(@RequestParam("conversation_id") String conversationId,
                                  @RequestParam("user_id") long userId,
                                  @RequestBody Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setItemCount(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getSQLStart(), page.getLimit());

        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.getUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId, userId));

        // 将私信列表中的未读消息改为已读
        List<Long> ids = getUnreadLetterIds(letterList, userId);
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
            System.out.println("Successfully read message : " + ids.size());
        }

        return "/site/letter-detail";
    }


    /**
     * 获取私信对方对象
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId, long userId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (userId == id0) {
            return userService.getUserById(id1);
        }
        else {
            return userService.getUserById(id0);
        }
    }

    /**
     * 获取当前登录用户未读私信的 id
     * @param letterList
     * @return
     */
    private List<Long> getUnreadLetterIds(List<Message> letterList, long userId) {
        List<Long> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                // 当前用户是私信的接收者且该私信处于未读状态
                if (userId == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }


    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(@RequestParam("target_id") long targetId,
                             @RequestParam("from_id") long fromId,
                             @RequestParam("content") String content) {
        // Integer.valueOf("abc"); // 测试统一异常处理（异步请求）
        User target = userService.getUserById(targetId);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在");
        }

        Message message = new Message();
        message.setFromId(fromId);
        message.setToId(target.getId());
        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        }
        else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setStatus(0); // 默认就是 0 未读，可不写
        message.setCreateTime(System.currentTimeMillis());

        messageService.addMessage(message);
        System.out.println("Successfully send message ! " + message.getId());
        return CommunityUtil.getJSONString(0);
    }

    @PostMapping("/notice/list")
    public String getNoticeList(@RequestParam("userId")long userId,
                                Model model) {
        User user = userService.getUserById(userId);

        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), ServiceConstants.TOPIC_COMMENT);
        // 封装通知需要的各种数据
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();

            messageVO.put("message", message);

            String content = message.getContent();
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.getUserById(new Long((Integer)data.get("userId"))));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            long count = messageService.findNoticeCount(user.getId(), ServiceConstants.TOPIC_COMMENT);
            messageVO.put("count", count);

            long unread = messageService.findNoticeUnReadCount(user.getId(), ServiceConstants.TOPIC_COMMENT);
            messageVO.put("unread", unread);

            model.addAttribute("commentNotice", messageVO);
        }

        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), ServiceConstants.TOPIC_LIKE);
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();

            messageVO.put("message", message);

            String content = message.getContent();
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.getUserById(new Long((Integer)data.get("userId"))));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            long count = messageService.findNoticeCount(user.getId(), ServiceConstants.TOPIC_LIKE);
            messageVO.put("count", count);

            long unread = messageService.findNoticeUnReadCount(user.getId(), ServiceConstants.TOPIC_LIKE);
            messageVO.put("unread", unread);

            model.addAttribute("likeNotice", messageVO);
        }

        // 查询未读消息数量
        long letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        long noticeUnreadCount = messageService.findNoticeUnReadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        System.out.println("Unread notices : " + letterUnreadCount + " unread messages : " + noticeUnreadCount);
        return "/site/notice";
    }

    /**
     * 查询某个主题所包含的通知列表
     * @param topic
     * @param page
     * @param model
     * @return
     */
    @PostMapping("/notice/detail")
    public String getNoticeDetail(@RequestParam("topic") String topic,
                                  @RequestParam("userId") long userId,
                                  @RequestBody Page page, Model model) {
        User user = userService.getUserById(userId);

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setItemCount(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic,page.getSQLStart(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = notice.getContent();
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.getUserById(new Long((Integer)data.get("userId"))));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 发送系统通知的作者
                map.put("fromUser", userService.getUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        // 设置已读
        List<Long> ids = getUnreadLetterIds(noticeList, userId);
        if (!ids.isEmpty()) {

            messageService.readMessage(ids);
            System.out.println("Successfully read notice" + ids.size());
        }

        return "/site/notice-detail";
    }

}
