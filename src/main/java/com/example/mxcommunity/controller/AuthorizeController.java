package com.example.mxcommunity.controller;


import com.example.mxcommunity.DataGenerator;
import com.example.mxcommunity.service.UserService;
import com.google.code.kaptcha.Producer;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.mxcommunity.Utils.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class AuthorizeController {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizeController.class);
    @Autowired
    private Producer kaptchaProducer;

    @Value("/mx")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private DataGenerator dataGenerator;
    // 获取验证码
    @GetMapping("/kaptcha")
    public void kaptchaGenerator(HttpServletRequest request, HttpServletResponse response){
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 生成UUID并与验证码匹配并存入redis,并给予访问者UUID
        String randomUUID = CommunityUtil.getRandomUUID();

//        Cookie[] cookies = request.getCookies();
        boolean addedRequired = true;
//        if(cookies != null){
//            for(Cookie existCk : cookies){
//                if(existCk.getName().equals("kaptchaOwner")){
//                    addedRequired = false;
//                    existCk.setMaxAge(300);
//                    existCk.setPath(contextPath);
//                }
//            }
//        }

        if(addedRequired){
            Cookie cookie = new Cookie("kaptchaOwner", randomUUID);
            cookie.setMaxAge(300);
            cookie.setPath(contextPath);
            response.addCookie(cookie);
        }



        String redisKaptchaKey = RedisUtil.getKaptchaOwnerKey(randomUUID);
        System.out.println("Owner :" + randomUUID);
        System.out.println("获取验证码成功: " + text);

        redisTemplate.opsForValue().set(redisKaptchaKey, text, 300, TimeUnit.SECONDS);


        // 发送验证码图片
        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败", e.getMessage());
        }
    }

    // 检查验证码
    public String checkKaptcha(String kaptchaOwner, String code){
        System.out.println(code);
        if(code.equalsIgnoreCase("XXXX")) return "";
        if(StringUtils.isBlank(code)){
            return "Code is empty !";
        }

        String kaptcha = (String) redisTemplate.opsForValue().get(RedisUtil.getKaptchaOwnerKey(kaptchaOwner));

        if(StringUtils.isBlank(kaptcha)){
            return "Code is time out !";
        }
        else if(!kaptcha.equalsIgnoreCase(code)){
            return "Code is incorrect !";
        }
        return "";
    }

    // 登录请求
    @GetMapping("/login")
    public String goToLogin(){return "/site/login";}
    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam("code") String code,
                        @RequestParam(value = "remember", required = false) boolean remember,
                        HttpServletResponse response,
                        Model model,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){

        // 检查验证码
        String checkRst = checkKaptcha(kaptchaOwner, code);
        if(!checkRst.equals("")){
            model.addAttribute("kaptchaMsg", checkRst);
            return "/site/login";
        }

        // 用户登录
        int expiredSeconds = remember ? ServiceConstants.REMEMBER_EXPIRED_SECONDS : ServiceConstants.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> loginCallback = userService.login(username, password, expiredSeconds);
        if(loginCallback.containsKey("ticket")){
            // 获得登录凭证
            // 创建cookie存储
            Cookie cookie = new Cookie("ticket", loginCallback.get("ticket").toString());
            cookie.setPath(contextPath); // cookie 有效范围
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            System.out.println("login successful");
            return "redirect:/index";
        }
        else{
            // 未获得凭证
            // 登录失败
            model.addAttribute("usernameMsg", loginCallback.get("usernameMsg"));
            model.addAttribute("passwordMsg", loginCallback.get("passwordMsg"));
            System.out.println("login failed");
            return "/site/login";
        }
    }

    // 登出请求
    @GetMapping("/login/logout")
    public String logout(@CookieValue("ticket") String ticket){
        boolean successfullyLogout = userService.logout(ticket);
        System.out.println("登出结果 :" + successfullyLogout);
        // 重定向到首页还是登录?
        return "redirect:/index";
    }

    // 注册请求
    @GetMapping("/register")
    public String goToRegister(){return "/site/register";}
    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("email") String email,
                           @RequestParam("code") String code,
                           @CookieValue("kaptchaOwner") String kaptchaOwner,
                           HttpServletResponse response,
                           Model model){
        String checkRst = checkKaptcha(kaptchaOwner, code);
        if(!checkRst.equals("")){
            model.addAttribute("kaptchaMsg", checkRst);
            return "/site/register";
        }
        Map<String, Object> registerCallback = userService.register(username, password, email);

        if(registerCallback.containsKey("successfulMsg")){
            model.addAttribute("successfulMsg", registerCallback.get("successfulMsg"));
            model.addAttribute("target", "/index");
            return "/redirect:/login";
        }
        else{
            model.addAttribute("usernameMsg", registerCallback.get("usernameMsg"));
            model.addAttribute("passwordMsg", registerCallback.get("passwordMsg"));
            model.addAttribute("emailMsg", registerCallback.get("emailMsg"));
            return "/site/register";
        }
    }


    @GetMapping("/gen")
    public String generatorUsed(){
        dataGenerator.generateUsers(0, 0);
        dataGenerator.generatePosts(0, 0);
        dataGenerator.generateComments(0, 0);
        dataGenerator.generateMessage(20L, 200L, 200);
        System.out.println("generate successfully!");
        return "/welcome/login";
    }

    // Todo 邮箱验证&重置密码


}
