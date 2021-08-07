package com.Long.service;

/**
 * @program: Dubbo
 * @description: mq模块接口
 * @author: 寒风
 * @create: 2021-08-07 09:13
 **/
public interface mqService {
    //事务型同步库存扣减消息
    boolean transactionAsyncReduceStock(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId);

    boolean asyncReduceStock(Integer itemId, Integer amount);
}
