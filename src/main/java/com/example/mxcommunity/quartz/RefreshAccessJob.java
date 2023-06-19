package com.example.mxcommunity.quartz;

import com.example.mxcommunity.Utils.RedisUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;


public class RefreshAccessJob implements Job {

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

//    @Autowired
//    private JobSetSelection selection;

    public RefreshAccessJob(){
//        String s = selection.getCurrentKey();
//        selection.alterCurrentKey();
        System.out.println("a new refreshAccessjob create !");
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String accessKey = RedisUtil.getAccessKey();
        // BoundSetOperations operations = redisTemplate.boundSetOps(heatPostKey);
        Map<String, Object> map = redisTemplate.opsForHash().entries(accessKey);
        Set<Map.Entry<String, Object>> operations = map.entrySet();
        if (operations.size() == 0) {
            logger.info("任务取消, 因为没有访问量需要刷新");
            System.out.println("no post access need to refresh");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子访问量: " + operations.size());
        System.out.println("begin refresh !");
//        while (operations.size() > 0) {
//            this.refreshAccess(new Long((Integer) operations.pop()));
//        }
        for(Map.Entry<String, Object> op : operations){
            this.refreshAccess(RedisUtil.getReverseAccessHashKey(op.getKey()), new Long((Integer) op.getValue()));
        }
        logger.info("[任务结束] 帖子访问量刷新完毕");
        System.out.println("access fresh ends !");
    }

    public void refreshAccess(long postId, long increment){
        System.out.println("Simulate an access db op at " + postId + " add " + increment + " !");
    }
}

