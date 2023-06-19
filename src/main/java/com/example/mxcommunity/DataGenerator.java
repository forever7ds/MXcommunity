package com.example.mxcommunity;

import com.example.mxcommunity.Utils.CommentConstants;
import com.example.mxcommunity.entity.model.Comment;
import com.example.mxcommunity.entity.model.Message;
import com.example.mxcommunity.entity.model.ThemePost;
import com.example.mxcommunity.entity.model.User;
import com.example.mxcommunity.service.CommentService;
import com.example.mxcommunity.service.MessageService;
import com.example.mxcommunity.service.ThemePostService;
import com.example.mxcommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class DataGenerator {

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ThemePostService themePostService;

    @Autowired
    private MessageService messageService;

    public void generateUsers(int begin, int end){
        for(int i=begin;i<end;i++){
            userService.register("GenUsername" + i, "GenPassword" + i, "GenEmail" + i + "@163.com");
        }
        System.out.println("generate " + (end-begin) + "users");
    }

    public void generatePosts(int begin, int end){
        List<User> users = userService.getUsersList();
        for(int i=begin;i<end;i++){
            ThemePost post = new ThemePost();
            post.initDefaultField();
            post.setContent("GenContent"+i);
            post.setTitle("GenTitle"+i);
            post.setCreatorId(users.get(new Random().nextInt(users.size())).getId());
            post.setTag("#GenEmptyTag");
            post.setType(0);
            themePostService.publishNewPost(post);
        }
        System.out.println("generate " + (end-begin) + "posts");
    }

    public void generateComments(int begin, int end){
        List<ThemePost> themePosts = themePostService.getThemePostsList();
        List<User> users = userService.getUsersList();
        for(int i=begin;i<end;i++){
            Comment comment = new Comment();
            comment.initDefaultField();
            comment.setContent("GeneratePost-Comment" + i);
            comment.setCreatorId(users.get(new Random().nextInt(users.size())).getId());
            ThemePost randomPost = themePosts.get(new Random().nextInt(themePosts.size()));
            comment.setTargetId(randomPost.getCreatorId());
            comment.setEntityId(randomPost.getId());
            comment.setEntityType(CommentConstants.COMMENT_ENTITY_TYPE_POST);
            commentService.addComment(comment);
        }

        System.out.println("generate " + (end-begin) + "comments");

        List<Comment> comments = commentService.getCommentsList();
        for(int i=begin;i<end*3;i++){
            Comment comment = new Comment();
            comment.initDefaultField();
            comment.setContent("GenerateComment-Comment" + i);
            comment.setCreatorId(users.get(new Random().nextInt(users.size())).getId());
            Comment randomComment = comments.get(new Random().nextInt(themePosts.size()));
            comment.setTargetId(randomComment.getCreatorId());
            comment.setEntityId(randomComment.getId());
            comment.setEntityType(CommentConstants.COMMENT_ENTITY_TYPE_COMMENT);
            commentService.addComment(comment);
        }

        System.out.println("generate " + (end*3-begin) + "subComments");
    }

    public void generateMessage(long beginId, long endId, int conversationSum){
        for(int i=0;i<conversationSum;i++){
            long fromId = (new Random().nextInt(Long.valueOf(endId-beginId).intValue()))+beginId;
            long toId = fromId + new Random().nextInt(20) - 10;
            if(toId == fromId) toId--;
            for(int j=0;j<new Random().nextInt(15);j++){
                Message msg = new Message();
                msg.initDefaultField();
                if(new Random().nextInt(10) < 5){
                    msg.setToId(toId);
                    msg.setFromId(fromId);
                }
                else{
                    msg.setToId(fromId);
                    msg.setFromId(toId);
                }
                if(fromId < toId){
                    msg.setConversationId(fromId + "_" + toId);
                }
                else {
                    msg.setConversationId(toId + "_" + fromId);
                }
                msg.setContent("genMessageConservation" + i + "msg" +  j);
                messageService.addMessage(msg);
            }

        }
    }

}
