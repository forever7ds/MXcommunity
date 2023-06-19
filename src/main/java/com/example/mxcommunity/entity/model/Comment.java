package com.example.mxcommunity.entity.model;

import com.example.mxcommunity.Utils.CommentConstants;
import lombok.Data;

@Data
public class Comment {
    private long id;

    private long creatorId;

    private long entityId;

    private long createTime;

    private int status;

    private String content;

    private int entityType;

    private int likeCount;

    private long targetId;

    public void initDefaultField(){
        this.likeCount = 0;
        this.status = CommentConstants.COMMENT_STATUS_NORMAL;
        this.createTime = System.currentTimeMillis();
    }
}
