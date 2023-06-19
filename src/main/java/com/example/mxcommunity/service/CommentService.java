package com.example.mxcommunity.service;

import com.example.mxcommunity.Utils.CommentConstants;
import com.example.mxcommunity.Utils.RedisUtil;
import com.example.mxcommunity.controller.CommentController;
import com.example.mxcommunity.dao.CommentMapper;
import com.example.mxcommunity.entity.Page;
import com.example.mxcommunity.entity.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    RedisTemplate redisTemplate;

    public List<Comment> getCommentsByPostId(long postId) {
        return commentMapper.selectCommentsByEntityId(postId, CommentConstants.COMMENT_ENTITY_TYPE_POST);
    }
    public List<Comment> getPagingCommentsByPostId(long postId, Page page) {

        return commentMapper.selectPagingCommentsByEntityId(postId, CommentConstants.COMMENT_ENTITY_TYPE_POST, page.getSQLStart(), page.getLimit());
    }
    public List<Comment> getCommentsByCommentId(long commentId) {
        return commentMapper.selectCommentsByEntityId(commentId, CommentConstants.COMMENT_ENTITY_TYPE_COMMENT);
    }

    public Comment getCommentById(long id){
        return commentMapper.selectCommentById(id);
    }
    public void updateStatusById(long id, int commentStatus) {
        commentMapper.updateStatusById(id, commentStatus);
    }

    public Map<String, Object> addComment(Comment addedComment) {
        Map<String, Object> callback = new HashMap<>();
        commentMapper.insertComment(addedComment);
        callback.put("successfulMsg", "添加评论成功!");
        return callback;
    }

    public List<Comment> getCommentsList(){ return commentMapper.selectAllComments(); }
}
