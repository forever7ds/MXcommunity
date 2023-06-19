package com.example.mxcommunity.dao;


import com.example.mxcommunity.entity.model.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    List<Message> selectConversations(long userId, int offset, int limit);

    /**
     * 查询当前用户的会话数量
     * @param userId
     * @return
     */
    long selectConversationCount(long userId);

    /**
     * 查询某个会话所包含的私信列表
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectLetters(String conversationId, int offset, int limit);

    /**
     * 查询某个会话所包含的私信数量
     * @param conversationId
     * @return
     */
    long selectLetterCount(String conversationId);

    /**
     * 查询未读私信的数量
     * @param userId
     * @param conversationId conversationId = null, 则查询该用户所有会话的未读私信数量
     *                        conversationId != null, 则查询该用户某个会话的未读私信数量
     * @return
     */
    long selectLetterUnreadCount(long userId, String conversationId);

    /**
     * 修改消息的状态
     * @param ids
     * @param status
     * @return
     */
    long updateStatus(List<Long> ids, int status);

    /**
     * 新增一条私信
     * @param message
     * @return
     */
    long insertMessage(Message message);

    /**
     * 查询某个主题下最新的通知
     * @param userId
     * @param topic
     * @return
     */
    Message selectLatestNotice(long userId, String topic);

    /**
     * 查询某个主题下包含的系统通知数量
     * @param userId
     * @param topic
     * @return
     */
    long selectNoticeCount(long userId, String topic);

    /**
     * 查询未读的系统通知数量
     * @param userId
     * @param topic
     * @return
     */
    long selectNoticeUnReadCount(long userId, String topic);

    /**
     * 查询某个主题所包含的通知列表
     * @param userId
     * @param topic
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectNotices(long userId, String topic, int offset, int limit);
}
