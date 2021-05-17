package com.Long.Service;

/**
 * @program: Seckill
 * @description: 本地热点缓存的服务
 * @author: 寒风
 * @create: 2021-04-05 18:17
 **/
public interface CashService {
    // 存cache
    void setCommonCache(String key,Object value);
    // 取cache
    Object getFromCommonCache(String key);
}
