package com.example.mxcommunity.entity.model;

import com.example.mxcommunity.Utils.CommunityUtil;
import com.example.mxcommunity.Utils.UserConstants;
import lombok.Data;

import java.io.Serializable;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Random;

@Data
public class User implements Serializable {
    private long id;

    private String username;

    private String name;

    private String password;

    private int status;

    private String introduction;

    private String email;

    private String avatarUrl;

    private String token;

    private int exp;

    private long createTime;

    private String salt;

    public void initDefaultField(){
        this.status = UserConstants.USER_STATUS_NORMAL;
        this.token = CommunityUtil.getRandomUUID();
        this.name = "default-" + this.token;
        this.avatarUrl = String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000));
        this.createTime = System.currentTimeMillis();
        this.salt = CommunityUtil.getRandomUUID().substring(0, 5);
    }

}
