package com.foo.community.config;

import com.foo.community.controller.interceptor.LoginRequiredInterceptor;
import com.foo.community.controller.interceptor.LoginTicketInterceptor;
import com.foo.community.controller.interceptor.TestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //测试拦截器注入的
    @Autowired
    private TestInterceptor testInterceptor;

    //注入登录拦截器
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Resource
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*jpeg");

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*jpeg");

    }
}
