package com.foo.community.controller;

import com.foo.community.entity.User;
import com.foo.community.service.UserService;
import com.foo.community.util.CommunityConstant;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.jws.WebParam;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;

@Controller
public class LoginController implements CommunityConstant {

    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private Producer kaptchaProducer;

    @GetMapping("/register")
    public String getRegisterPage(){
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage(){
        return "/site/login";
    }

    @GetMapping("/letter")
    public String getLetterPage(){
        return "/site/letter";
    }

    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        //map里面包含处理信息，下面用model封装
        if (map==null || map.isEmpty()) {
            model.addAttribute("msg","注册成功，我们以及向您的邮箱发送了一份邮件，请尽快激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg","激活成功，您的账户已经可以正常使用了");
            model.addAttribute("target","/login");
        } else if(result == ACTIVATION_REPEAT) {
            model.addAttribute("msg","无效操作，该账户已经激活过了");
            model.addAttribute("target","/index");
        } else {
            model.addAttribute("msg","激活失败，您提供的激活码不正确！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("/kaptcha")
    public void getkapthca(HttpServletResponse response, HttpSession session) {
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存入session
        session.setAttribute("kaptcha",text);

        response.setContentType("image/png");

        OutputStream os = null;
        try {
            os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("验证码响应失败"+e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(String username,String password,String code,boolean rememberme,
                        Model model,HttpSession session,HttpServletResponse response) {
        //先判断验证码，如果验证码不对，不用看，直接不能登录，验证码是存在session里的
        String  kaptcha =(String) session.getAttribute("kaptcha");
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            //equalsIgnoreCase忽略大小写
            model.addAttribute("codeMsg","验证码不正确");
            return "site/login";
        }

        //验证码没问题，就检查账号，密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);//cookie的生效路径，这里设置在则整个项目
            cookie.setMaxAge(expiredSeconds); //cookie的有效时间
            response.addCookie(cookie); //把cookie添加到回复请求中
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }

}
