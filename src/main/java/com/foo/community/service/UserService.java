package com.foo.community.service;

import com.foo.community.dao.LoginTicketMapper;
import com.foo.community.dao.UserMapper;
import com.foo.community.entity.LoginTicket;
import com.foo.community.entity.User;
import com.foo.community.util.CommunityConstant;
import com.foo.community.util.CommunityUtil;
import com.foo.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService implements CommunityConstant {

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String path;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private MailClient mailClient;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserByUserId(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //验证用户信息不为空
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }
        //验证密码不为空
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        //验证邮箱不为空
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        //都不为空，验证账户名是否已存在
        User u = userMapper.selectByUserName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "用户名已存在");
            return map;
        }

        //验证邮箱是否已被注册
        List<User> list = userMapper.selectByEmail(user.getEmail());
        if (list != null) {
            map.put("emailMsg", "邮箱已被注册过了");
//            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.newcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insert(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //url:http://localhost:8081/community/activation/101/code
        String url = domain + path + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,int expiredSeconds) {
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)) {
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        User user = userMapper.selectByUserName(username);
        //1.用户不能存在
        if(user == null) {
            map.put("usernameMsg","该用户不存");
            return map;
        }
        //2.用户未激活
        if(user.getStatus() == 0) {
            map.put("usernameMsg","用户未激活");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if(!password.equals(user.getPassword())) {
            map.put("passwordMsg","密码错误");
            return map;
        }

        //上面的代码如果都未执行,说明登录成功,下面进行生成登录凭证loginticket（id，userId,status,ticket,expired）
        //这个ticket就相当于session，不过这里使用数据库完成的
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);
    }

    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateheader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }

    //根据用户名查询用户
    public User findUserByUserName(String username) {
        return userMapper.selectByUserName(username);
    }
}