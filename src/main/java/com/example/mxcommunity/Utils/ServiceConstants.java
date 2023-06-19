package com.example.mxcommunity.Utils;

public interface ServiceConstants {

    int DEFAULT_EXPIRED_SECONDS = 60*60*24;

    int REMEMBER_EXPIRED_SECONDS = 60*60*24*30;

    // Kafka 主题：评论
    String TOPIC_COMMENT = "comment";

    // Kafka 主题：点赞
    String TOPIC_LIKE = "like";

    // Kafka 主题：关注
    String TOPIC_FOLLOW = "follow";

    // Kafka 主题：发帖
    String TOPIC_PUBLISH = "publish";

    // Kafka 主题：删帖
    String TOPIC_DELETE = "delete";

    long SYSTEM_USER_ID = 1L;



}
