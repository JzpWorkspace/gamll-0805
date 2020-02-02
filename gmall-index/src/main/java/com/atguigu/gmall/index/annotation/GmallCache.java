package com.atguigu.gmall.index.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    //自定义缓存的key的值
    String value() default "";
    //自定义缓存的有效时间
    int timeout() default 30;
    //防止服务器的雪崩，设置范围。
    int bound() default 5;
    //所锁的名字
    String lockName() default "lock";

    /* 注解本身没有逻辑，通过AOP赋予它们逻辑*/
                 /*  环绕通知  */
}
