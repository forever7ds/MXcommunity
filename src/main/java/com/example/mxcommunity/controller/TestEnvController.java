package com.example.mxcommunity.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestEnvController {

    @GetMapping("/message")
    public Map<String,String> testEnvironment(){
        Map<String, String> map = new HashMap<>();
        map.put("First message", "Hello world!");
        return map;
    }
}
