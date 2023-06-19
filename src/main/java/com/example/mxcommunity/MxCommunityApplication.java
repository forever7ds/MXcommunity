package com.example.mxcommunity;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
// @RestController
// @MapperScan("com.example.mxcommunity.dao")
public class MxCommunityApplication {
//    @GetMapping("/test")
//    public Map<String,String> testEnvironment(){
//        Map<String, String> map = new HashMap<>();
//        map.put("First message", "Hello World !");
//        return map;
//    }

    // public static ConfigurableApplicationContext ac;

    @PostConstruct
    public void init() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }
    public static void main(String[] args) {
        SpringApplication.run(MxCommunityApplication.class, args);
    }

}
