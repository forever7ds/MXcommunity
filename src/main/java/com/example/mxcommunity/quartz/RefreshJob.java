package com.example.mxcommunity.quartz;

import com.example.mxcommunity.Utils.CommentConstants;
import com.example.mxcommunity.Utils.RedisUtil;
import com.example.mxcommunity.entity.model.ThemePost;
import com.example.mxcommunity.service.JobSetService;
import com.example.mxcommunity.service.LikeService;
import com.example.mxcommunity.service.ThemePostService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;



public class RefreshJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(RefreshJob.class);

    private static final long epoch;


    static {
        try {
            // epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-01-01 00:00:00");
            // epoch = System.currentTimeMillis();
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-02-25 00:00:00");
            epoch = date.getTime();
        } catch (Exception e) {
            throw new RuntimeException("初始化 Epoch 纪元失败", e);
        }
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThemePostService themePostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private JobSetService selection;

    public RefreshJob(){
         System.out.println(" job create !");
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisSelectKey = selection.getCurrentKey();
        selection.alterCurrentKey();
        System.out.println("a new refresh job create and select key is : " + redisSelectKey);

        String heatPostKey = RedisUtil.getHeatPostsKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(heatPostKey);

        if(operations.size() == 0){
            logger.info("任务取消, 因为没有帖子需要刷新");
            System.out.println("no post need to refresh");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数: " + operations.size());
        System.out.println("begin refresh !");
        while (operations.size() > 0) {
            this.refreshScore(new Long((Integer) operations.pop()));
        }
        logger.info("[任务结束] 帖子分数刷新完毕");
        System.out.println("fresh ends !");



    }

    public void refreshScore(long postId){
        ThemePost themePost = themePostService.getPostDetailById(postId);
        if (themePost == null) {
            logger.error("该帖子不存在: id = " + postId);
            return ;
        }


        // 是否加精
        boolean wonderful = themePost.getType() == 1;
        // 评论数量
        int commentCount = themePost.getCommentCount();
        // 点赞数量
        long likeCount = likeService.getEntityLikedCount(CommentConstants.COMMENT_ENTITY_TYPE_POST, postId);

        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;

        double score = Math.log10(Math.max(w, 1))
                + (themePost.getModifiedTime() - epoch) / (1000 * 3600 * 24);

        themePostService.updateScoreById(postId, score);

    }
}
