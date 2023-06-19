package com.example.mxcommunity.Utils;


import com.example.mxcommunity.entity.model.User;
import org.springframework.stereotype.Component;

@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    // 存储 User
    public void setUser(User user) {
        users.set(user);
    }

    // 获取 User
    public User getUser() {
        return users.get();
    }

    // 清理
    public void clear() {
        users.remove();
    }
}
