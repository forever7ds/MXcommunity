package com.example.mxcommunity.entity.model;

import com.example.mxcommunity.Utils.ThemePostConstants;
import lombok.Data;

@Data
public class ThemePost {
    private long id;

    private long creatorId;

    private long createTime;

    private long modifiedTime;

    private String title;

    private String content;

    private String tag;

    private int type;

    private int commentCount;

    private int likeCount;

    private int favouriteCount;

    private int viewCount;

    private double heatScore;

    public void initDefaultField(){
        this.createTime = System.currentTimeMillis();
        this.modifiedTime = System.currentTimeMillis();
    }
}
