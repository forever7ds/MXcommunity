package com.example.mxcommunity.dao;

import com.example.mxcommunity.entity.model.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntityId(long entityId, int entityType);

    List<Comment> selectPagingCommentsByEntityId(long entityId, int entityType, int start, int offset);

    long updateStatusById(long id, int commentStatus);

    long insertComment(Comment addedComment);

    Comment selectCommentById(long id);

    List<Comment> selectAllComments();
}
