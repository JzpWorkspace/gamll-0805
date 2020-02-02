package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import sun.reflect.generics.tree.ReturnType;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
       //首先获取注解对象
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        GmallCache annotation = signature.getMethod().getAnnotation(GmallCache.class);
        Class returnType = signature.getReturnType();
        List<ProceedingJoinPoint> joinPoints = Arrays.asList(joinPoint);
        //1.获取缓存数据
        String prefix = annotation.value();
        String key = prefix+joinPoints;
        Object cache = this.getCache(key, returnType);
        if (cache != null) {
            return cache;
        }
        //3.数据是空，空就加锁
        String lockName = annotation.lockName();
        RLock fairLock = this.redissonClient.getFairLock(lockName + joinPoints);
        fairLock.lock();
        //4.判断缓存是否为空
        Object cache1 = this.getCache(key, returnType);
        if (cache1 != null) {
            fairLock.unlock();
            return cache1;
        }
        Object result = joinPoint.proceed(joinPoint.getArgs());
        //数据放入缓存
        int bound = annotation.bound();
        int timeout = annotation.timeout();
        this.redisTemplate.opsForValue().set(key,JSON.toJSONString(result), timeout+new Random().nextInt(bound), TimeUnit.MINUTES);
        fairLock.unlock();
        return result;


    }
    private Object getCache(String key,Class returnType){
        String jsonString = this.redisTemplate.opsForValue().get(key);
        //2.有就返回
        if(StringUtils.isNotBlank(jsonString)){
            return JSON.parseObject(jsonString, returnType);
        }
        return null;
    }
}
