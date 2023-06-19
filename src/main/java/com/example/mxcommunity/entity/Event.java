package com.example.mxcommunity.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Event {

    private String topic; // 事件类型
    private long userId; // 事件由谁触发
    private int entityType; // 实体类型
    private long entityId; // 实体 id
    private long entityUserId; // 实体的作者(该通知发送给他）
    private Map<String, Object> data = new HashMap<>(); // 存储未来可能需要用到的数据

}