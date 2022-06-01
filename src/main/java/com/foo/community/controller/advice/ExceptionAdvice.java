package com.foo.community.controller.advice;

import com.foo.community.util.CommunityUtil;
import com.mysql.cj.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

//全局异常通知
@ControllerAdvice(annotations = Controller.class) //通知
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常" + e.getMessage());
        //异常信息遍历
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        //异步请求？浏览器需要json而不是字符串
        String xRequestWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestWith)) {
            //这是一个异步请求
            response.setContentType("application/json;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常"));
        } else {
            //重定向
            response.sendRedirect(request.getContextPath() + "/error"); //请求中项目路径 + error
        }
    }
}
