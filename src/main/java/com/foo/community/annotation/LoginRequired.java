package com.foo.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) //用来描述方法
@Retention(RetentionPolicy.RUNTIME) //注解在运行时生效
public @interface LoginRequired {

}
