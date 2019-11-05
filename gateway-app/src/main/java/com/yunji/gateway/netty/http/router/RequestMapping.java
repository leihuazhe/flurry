package com.yunji.gateway.netty.http.router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Denim.leihz 2019-11-04 10:16 AM
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    /**
     * 路由的uri
     */
    String uri();

    /**
     * 路由的方法
     */
    String method();
}

