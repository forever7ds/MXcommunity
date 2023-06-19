package com.example.mxcommunity.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class OnlineTicket implements Serializable {
    private String ticket;

    private long userId;

    private int onlineStatus;

    private int expiredSeconds;

}
