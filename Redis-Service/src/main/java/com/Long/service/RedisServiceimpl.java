package com.Long.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @program: Dubbo
 * @description:
 * @author: 寒风
 * @create: 2021-08-06 11:53
 **/
@org.springframework.stereotype.Service
@Service(timeout = 1000,retries = 3,interfaceClass = RedisServive.class)//暴露服务 动态代理使用的是接口的实现类
public class RedisServiceimpl implements RedisServive {

    private Cache<String,Object> commonCache = null;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void setKey(Object k,Object v){
        redisTemplate.opsForValue().set(k,v);
    }
    @Override
    public Object getKey(Object k){return redisTemplate.opsForValue().get(k);}
    @Override
    public void expire(Object k, long v, TimeUnit unit){redisTemplate.expire(k,v,unit);}
    @Override
    public Long increment(Object k,long v){
        return redisTemplate.opsForValue().increment(k, v);
    }
    @Override
    public boolean hasKey(Object k){return redisTemplate.hasKey(k);}

    @PostConstruct // 加载bean时优先执行该方法
    public void init(){
        commonCache = CacheBuilder.newBuilder()
                //设置缓存容器的初始容量为10
                .initialCapacity(10)
                //设置缓存中最大可以存储100个KEY,超过100个之后会按照LRU的策略移除缓存项
                .maximumSize(100)
                //设置写缓存后多少秒过期
                .expireAfterWrite(60, TimeUnit.SECONDS).build();
    }

    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key,value);
    }

    @Override
    public Object getFromCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }
}
