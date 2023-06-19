package com.example.mxcommunity.service;

import com.example.mxcommunity.Utils.RedisUtil;
import com.example.mxcommunity.dao.ThemePostMapper;
import com.example.mxcommunity.entity.Page;
import com.example.mxcommunity.entity.model.ThemePost;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Objects;

@Service
public class ThemePostService {


    private static final Logger logger = LoggerFactory.getLogger(ThemePostService.class);
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    ThemePostMapper themePostMapper;

    private LoadingCache<String, List<ThemePost>> postListCache;

    // 帖子总数的本地缓存
    // key - userId(其实就是0,表示查询的是所有用户. 对特定用户的查询不启用缓存）
    private LoadingCache<Long, Long> postRowsCache;

    @Value("${caffeine.posts.max-size}")
    private int localCacheMaxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int localCacheExpireSeconds;


    public Map<String, Object> publishNewPost(ThemePost toPublishThemePost){
        Map<String, Object> callback = new HashMap<>();
        themePostMapper.insertThemePost(toPublishThemePost);
        callback.put("successfulMsg", "您的主题帖发布成功!");
        return callback;
    }



    @PostConstruct
    public void initLocalCache(){
        postListCache = Caffeine.newBuilder()
                .maximumSize(localCacheMaxSize)
                .expireAfterWrite(localCacheExpireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<ThemePost>>(){
                    @Nullable
                    @Override
                    public List<ThemePost> load(@NonNull String key) throws Exception{
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        logger.debug("load post list from DB");
                        System.out.println("load post list from DB");
                        return themePostMapper.selectThemePostsPagingByLatest(offset, limit);

                    }
                });

        postRowsCache = Caffeine.newBuilder()
                .maximumSize(localCacheMaxSize)
                .expireAfterWrite(localCacheExpireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Long, Long>() {
                    @Nullable
                    @Override
                    public Long load(@NonNull Long key) throws Exception {
                        logger.debug("load post rows from DB");
                        return (Long) themePostMapper.selectPostCount();
                    }
                });

        System.out.println("Caffeine cache init !" + postListCache.estimatedSize());
    }


    public ThemePost getPostDetailById(long id){
        return themePostMapper.selectThemePostById(id);
    }


    public boolean updateTypeById(long id, int type) {
        themePostMapper.updateTypeById(id, type);
        return true;
    }

    public long updateScoreById(long id, double score) {
        return themePostMapper.updateScoreById(id, score);
    }

    public List<ThemePost> getPagingPosts(Page page, int mode){
        if(mode == 0){
            return postListCache.get(page.getSQLStart()+":"+page.getLimit());
        }
        return themePostMapper.selectThemePostsPagingByOldest(page.getSQLStart(), page.getLimit());
    }

    public List<ThemePost> getPagingPostsByType(Page page, int mode, int type){
        if(mode == 0) return themePostMapper.selectThemePostsPagingByTypeLatest(page.getSQLStart(), page.getLimit(), type);
        return themePostMapper.selectThemePostsPagingByTypeOldest(page.getSQLStart(), page.getLimit(), type);
    }

    public long getCountPosts(){
        return postRowsCache.get(0L);
    }

    public List<ThemePost> getThemePostsList(){ return themePostMapper.selectAllThemePosts(); }

    public void updateModifiedTimeById(long entityId, long currentTimeMillis) {
        themePostMapper.updateModifiedTimeById(entityId, currentTimeMillis);
    }
}
