package com.example.mxcommunity.entity;


import lombok.Data;

@Data
public class Page {

    private int page;

    private int limit;

    private long itemCount;

    private String path;

    public int getSQLStart(){
        return page*limit - limit;
    }
}
