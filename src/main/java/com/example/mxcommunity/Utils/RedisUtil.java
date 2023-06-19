package com.example.mxcommunity.Utils;

public class RedisUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_KAPTCHA = "kaptcha";

    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER_ID = "user_id";

    private static final String PREFIX_NAME_OF_USER_ID = "name_of_user_id";

    private static final String PREFIX_HEAT_POST = "heat_posts";

    private static final String PREFIX_ENTITY_LIKED = "entity_type_and_id";

    private static final String PREFIX_USER_LIKED = "user_id_liked";

    private static final String PREFIX_ACCESS = "access_map";

    private static final String PREFIX_ACCESS_HASH = "access_post";
    public static String getKaptchaOwnerKey(String ownerTempId){return PREFIX_KAPTCHA + SPLIT + ownerTempId;}
    public static String getTicketKey(String ticket){return PREFIX_TICKET + SPLIT + ticket;}

    public static String getUserIdKey(long id){ return PREFIX_USER_ID + SPLIT + id; };

    public static String getNameOfUserIdKey(long id){ return PREFIX_USER_ID + SPLIT + id; };

    public static String getHeatPostsKey(){ return PREFIX_HEAT_POST; }

    public static String getEntityLikedKey(int entityType, long entityId){
        return PREFIX_ENTITY_LIKED + SPLIT + entityType + SPLIT + entityId;
    }

    public static String getUserLikedKey(long userId){
        return PREFIX_USER_LIKED + SPLIT + userId;
    }

    public static String getAccessKey(){ return PREFIX_ACCESS; }

    public static String getAccessHashKey(long postId){ return PREFIX_ACCESS_HASH + SPLIT + postId; }

    public static Long getReverseAccessHashKey(String HashKey){ return Long.valueOf(HashKey.split(":")[1]); }
}
