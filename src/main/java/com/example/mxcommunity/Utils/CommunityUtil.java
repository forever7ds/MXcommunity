package com.example.mxcommunity.Utils;
import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    public static String getRandomUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }


    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }


    // 重载 getJSONString 方法，服务端方法可能不返回业务数据
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    // 重载 getJSONString 方法，服务端方法可能不返回业务数据和提示消息
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

}
