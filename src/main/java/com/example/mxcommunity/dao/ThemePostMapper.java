package com.example.mxcommunity.dao;


import com.example.mxcommunity.entity.model.Comment;
import com.example.mxcommunity.entity.model.ThemePost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ThemePostMapper {

    public long insertThemePost(ThemePost themePost);

    public long updateTypeById(long id, int type);

    public long updateViewCountById(long id, int viewCount);

    public long updateFavouriteCountById(long id, int favouriteCount);

    public long updateLikeCountById(long id, int likeCount);

    public long updateCommentCountById(long id, int commentCount);

    public ThemePost selectThemePostById(long id);

    public List<ThemePost> selectThemePostsPagingByLatest(int start, int offset);

    public List<ThemePost> selectThemePostsPagingByOldest(int start, int offset);

    public List<ThemePost> selectThemePostsPagingByTypeLatest(int start, int offset, int type);

    public List<ThemePost> selectThemePostsPagingByTypeOldest(int start, int offset, int type);

    public List<ThemePost> selectAllThemePosts();

    public long selectPostCount();

    public long updateScoreById(long id, double score);

    public long updateModifiedTimeById(long id, long modifiedTime);
}
