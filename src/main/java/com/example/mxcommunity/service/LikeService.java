package com.example.mxcommunity.service;


import com.example.mxcommunity.Utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    public void addLike(long creatorId, int entityType, long entityId, long entityUserId, long postId){

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisUtil.getEntityLikedKey(entityType, entityId);
                String userLikeKey = RedisUtil.getUserLikedKey(entityUserId);

                // 判断用户是否已经点过赞了
                boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey, creatorId);

                redisOperations.multi(); // 开启事务

                if (isMember) {
                    // 如果用户已经点过赞，点第二次则取消赞
                    redisOperations.opsForSet().remove(entityLikeKey, creatorId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                }
                else {
                    redisTemplate.opsForSet().add(entityLikeKey, creatorId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }

                return redisOperations.exec(); // 提交事务
            }
        });
    }

    public int getUserLikedCount(long userId){
        Integer count = (Integer) redisTemplate.opsForValue().get(RedisUtil.getUserLikedKey(userId));
        return count == null ? 0 : count;
    }

    public long getEntityLikedCount(int entityType, long entityId){
        return redisTemplate.opsForSet().size(RedisUtil.getEntityLikedKey(entityType, entityId));
    }

    public boolean getUserLikedEntityOrNot(long userId, int entityType, long entityId){
        return redisTemplate.opsForSet().isMember(RedisUtil.getEntityLikedKey(entityType, entityId), userId);
    }
}
