package com.Long.service;

import java.util.concurrent.TimeUnit;

/**
 * @program: Dubbo
 * @description: 获取redis操作对象
 * @author: 寒风
 * @create: 2021-08-06 11:32
 **/
public interface RedisServive {
    void setKey(Object k,Object v);
    Object getKey(Object k);
    void expire(Object k, long v, TimeUnit unit);
    Long increment(Object k,long v);
    boolean hasKey(Object k);
    // 存cache
    void setCommonCache(String key, Object value);
    // 取cache
    Object getFromCommonCache(String key);
}
