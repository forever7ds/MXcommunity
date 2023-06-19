package com.example.mxcommunity.service;

import com.example.mxcommunity.Utils.CommunityUtil;
import com.example.mxcommunity.Utils.RedisUtil;
import com.example.mxcommunity.Utils.UserConstants;
import com.example.mxcommunity.dao.UserMapper;
import com.example.mxcommunity.entity.OnlineTicket;
import com.example.mxcommunity.entity.model.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> callback = new HashMap<>();
        // 格式验证
        if(StringUtils.isBlank(username)){
            callback.put("usernameMsg", "用户名不能为空!");
            return callback;
        }
        if(StringUtils.isBlank(password)) {
            callback.put("passwordMsg", "密码不能为空!");
            return callback;
        }
        // 数据库验证
        User user = userMapper.selectUserByUsername(username);
        if(user == null){
            callback.put("usernameMsg", "用户不存在!");
            return callback;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            callback.put("passwordMsg", "密码错误!");
            return callback;
        }
        if(user.getStatus() == UserConstants.USER_STATUS_BANNED){
            callback.put("usernameMsg", "用户已封禁!");
            return callback;
        }
        String ticket = CommunityUtil.getRandomUUID();
        // 缓存验证已登录与刷新
        OnlineTicket onlineTicket = new OnlineTicket();
        onlineTicket.setUserId(user.getId());
        onlineTicket.setTicket(ticket);
        onlineTicket.setOnlineStatus(0);
        onlineTicket.setExpiredSeconds(expiredSeconds);

        String ticketRedisKey = RedisUtil.getTicketKey(ticket);
        System.out.println("ticket_redis : " + ticketRedisKey);
        redisTemplate.opsForValue().set(ticketRedisKey, onlineTicket);

        callback.put("ticket", onlineTicket.getTicket());
        return callback;


    }

    public boolean logout(String ticket){
        String ticketRedisKey = RedisUtil.getTicketKey(ticket);
//        OnlineTicket onlineTicket = (OnlineTicket) redisTemplate.opsForValue().get(ticketRedisKey);
//        onlineTicket.setOnlineStatus(1);
//        redisTemplate.opsForValue().set(ticketRedisKey, onlineTicket, 0L, TimeUnit.SECONDS);
        redisTemplate.delete(ticketRedisKey);
        System.out.println("user : " + ticketRedisKey + "logout successfully!");
        return true;
    }

    public Map<String, Object> register(String username, String password, String email){
        Map<String, Object> callback = new HashMap<>();
        // 格式验证
        if(StringUtils.isBlank(username)){
            callback.put("usernameMsg", "用户名不能为空!");
            return callback;
        }
        if(StringUtils.isBlank(password)) {
            callback.put("passwordMsg", "密码不能为空!");
            return callback;
        }

        // 数据库验证是否存在
        User selectedUser = userMapper.selectUserByUsername(username);
        if(selectedUser != null){
            callback.put("usernameMsg", "用户名已被占用!");
            return callback;
        }
        selectedUser = userMapper.selectUserByEmail(email);
        if(selectedUser != null){
            callback.put("emailMsg", "邮箱已被占用!");
        }

        User addedUser = new User();
        addedUser.initDefaultField();
        addedUser.setUsername(username);
        addedUser.setPassword(CommunityUtil.md5(password + addedUser.getSalt()));
        addedUser.setEmail(email);

        userMapper.insertUser(addedUser);

        return callback;

    }

    public String getNameById(long id){
        return getUserById(id).getName();
    }

    public List<User> getUsersList(){ return userMapper.selectAllUsers(); }

    public User getUserById(long id){
        long redisTime = System.nanoTime();
        User user = getUserByIdFromCache(id);
        System.out.println("select id from redis time : " + (System.nanoTime() - redisTime));
        if(user != null){
            return user;
        }
        long dbTime = System.nanoTime();
        User dbuser = userMapper.selectUserById(id);
        System.out.println("select id from db time : " + (System.nanoTime() - dbTime));
        initUserCache(dbuser);
        return dbuser;
    }

    private User getUserByIdFromCache(long id){
        String userIdKey = RedisUtil.getUserIdKey(id);
        return (User) redisTemplate.opsForValue().get(userIdKey);
    }

    private void initUserCache(User user){
        String userIdKey = RedisUtil.getUserIdKey(user.getId());
        redisTemplate.opsForValue().set(userIdKey, user);
    }

    private void clearUserCache(long id){
        String userIdKey = RedisUtil.getUserIdKey(id);
        redisTemplate.delete(userIdKey);
    }

    public long getUserIdByTicket(String ticket){
        OnlineTicket ot = (OnlineTicket) redisTemplate.opsForValue().get(RedisUtil.getTicketKey(ticket));
        if(ot == null){
            return -1;
        }
        return ot.getUserId();
    }

    public OnlineTicket findLoginTicket(String ticket) {
        String ticketKey = RedisUtil.getTicketKey(ticket);
        OnlineTicket onlineTicket = (OnlineTicket) redisTemplate.opsForValue().get(ticketKey);
        return onlineTicket;
    }
}
