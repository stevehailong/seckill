package com.Long.service;

import com.Long.Error.BusinessException;
import com.Long.Model.PromoModel;

/**
 * @program: Seckill
 * @description: 秒杀活动业务逻辑接口
 * @author: 寒风
 * @create: 2021-03-29 06:35
 **/
public interface PromoService {
    /**
     * @description 通过商品id查询秒杀信息
     */
    PromoModel getPromoByItemId(Integer itemId);
    // 活动发布
    void publishPromo(Integer promoId) throws BusinessException;

    // 生成秒杀用的令牌
    String  generateSecondKillToken(Integer promoId, Integer itemId, Integer userId) throws BusinessException;
}