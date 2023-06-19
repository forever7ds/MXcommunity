package com.example.mxcommunity.entity.model;

import lombok.Data;

@Data
public class Message {
    private long id;
    private long fromId;
    private long toId;
    private String conversationId;
    private String content;
    private long createTime;
    private int status;
    public void initDefaultField(){
        this.createTime = System.currentTimeMillis();
    }
}
