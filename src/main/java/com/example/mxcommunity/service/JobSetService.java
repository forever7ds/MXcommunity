package com.example.mxcommunity.service;


import org.springframework.stereotype.Service;

@Service
public class JobSetService {

    private static String[] keyPool = new String[]{"Set1", "Set2"};

    private volatile String currentKey = keyPool[0];

    public JobSetService(){
        System.out.println("init successful!");
    }

    public String getCurrentKey(){ return currentKey; }

    public void alterCurrentKey(){
        if(currentKey.equals(keyPool[0])) currentKey = keyPool[1];
        else currentKey = keyPool[0];
    }

}
