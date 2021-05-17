package com.Long.Service.impl;

import com.Long.Service.CashService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @program: Seckill
 * @description: 本地热点数据缓存的实现
 * @author: 寒风
 * @create: 2021-04-05 18:21
 **/
@Service
public class CacheServiceImpl implements CashService {
    private Cache<String,Object> commonCache = null;

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
