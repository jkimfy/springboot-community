package com.foo.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//统一日志处理测试
//@Component
//@Aspect
public class TestAspect {

    //service.*.*(..) 第一个.*表示service组件里的所有类，第二个.*表示所有的方法，(..)表示参数
    //当然这些参数可以变为具体的类，具体的方法，具体的参数
    @Pointcut("execution(* com.foo.community.service.*.*(..))")
    public void pointcut() {

    }

    @Before("pointcut()")
    public void before() {
        System.out.println("before");
    }

    @After("pointcut()")
    public void after() {
        System.out.println("after");
    }

    //返回值以后再处理一些逻辑
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("调用组件方法之前做的事情");
        //调用目标组件的方法
        Object obj = joinPoint.proceed();
        System.out.println("调用组件方法之后做的事情");
        return obj;
    }
}
