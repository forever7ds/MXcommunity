package com.example.mxcommunity.interceptor;


import com.example.mxcommunity.Utils.HostHolder;
import com.example.mxcommunity.entity.model.User;
import com.example.mxcommunity.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    /**
     * Controller之后模板之前被调用
     * 获取未读私信/系统通知的数量
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            long letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            long noticeUnreadCount = messageService.findNoticeUnReadCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount", letterUnreadCount + noticeUnreadCount);
        }
    }
}
