package com.example.mxcommunity.service;


import com.example.mxcommunity.dao.MessageMapper;
import com.example.mxcommunity.entity.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;
    public long findConversationCount(long userId) {
        return messageMapper.selectConversationCount(userId);
    }

    public List<Message> findConversations(long id, int sqlStart, int limit) {
        return messageMapper.selectConversations(id, sqlStart, limit);
    }

    public long findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    public long findLetterUnreadCount(long id, String conversationId) {
        return messageMapper.selectLetterUnreadCount(id, conversationId);
    }

    public long findNoticeUnReadCount(long id, String topic) {
        return messageMapper.selectNoticeUnReadCount(id, topic);
    }

    public List<Message> findLetters(String conversationId, int sqlStart, int limit) {
        return messageMapper.selectLetters(conversationId, sqlStart, limit);
    }

    public void readMessage(List<Long> ids) {
        messageMapper.updateStatus(ids, 1);
    }

    public void addMessage(Message message) {
        messageMapper.insertMessage(message);
    }

    public Message findLatestNotice(long id, String topic) {
        return messageMapper.selectLatestNotice(id, topic);
    }

    public long findNoticeCount(long id, String topic) {
        return messageMapper.selectNoticeCount(id, topic);
    }

    public List<Message> findNotices(long id, String topic, int sqlStart, int limit) {
        return messageMapper.selectNotices(id, topic, sqlStart, limit);
    }
}
