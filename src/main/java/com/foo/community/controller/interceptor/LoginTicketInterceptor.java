package com.foo.community.controller.interceptor;

import com.foo.community.entity.LoginTicket;
import com.foo.community.entity.User;
import com.foo.community.service.UserService;
import com.foo.community.util.CookieUtil;
import com.foo.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getCookie(request, "ticket");
        if(ticket != null) {
            //查询登录凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查登陆凭证是否有效
            if (loginTicket != null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())) {
                User user = userService.findUserByUserId(loginTicket.getUserId());
                hostHolder.setUser(user);
            }

        }
        return true;
    }

    //controller之后，模板引擎之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //得到当前线程持有的user
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
