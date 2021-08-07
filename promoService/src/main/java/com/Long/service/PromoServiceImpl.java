package com.Long.service;

import com.Long.Error.BusinessException;
import com.Long.Model.ItemModel;
import com.Long.Model.PromoModel;
import com.Long.Model.UserModel;
import com.Long.dao.PromoMapper;
import com.Long.entity.Promo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @program: Seckill
 * @description: 秒杀逻辑的实现类
 * @author: 寒风
 **/
@Component
@com.alibaba.dubbo.config.annotation.Service(interfaceClass = PromoService.class)
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoMapper promoMapper;

    @Reference
    private RedisServive redisTemplate;

    @Reference
    private ItemService itemService;

    @Reference
    private UserService userService;

    /**
     * @description 通过商品id查询秒杀信息
     */
    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        Promo promo = promoMapper.selectByItemId(itemId);
        PromoModel promoModel = convertFromEntity(promo);
        if (promoModel == null) {
            return null;
        }
        //判断秒杀活动状态
        if (promoModel.getStartTime().isAfterNow()) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndTime().isBeforeNow()) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }
        return promoModel;
    }

    // 发布活动的逻辑
    @Override
    public void publishPromo(Integer promoId) throws BusinessException {
        // 通过活动id获取活动
        Promo promo = promoMapper.selectByPrimaryKey(promoId);
        if(promo.getItemId() == null || promo.getItemId() == 0){
            return;
        }
        ItemModel itemModel = itemService.getItemById(promo.getItemId());
        // 将库存同步到redis内
        redisTemplate.setKey("promo_item_stock_"+itemModel.getId(),itemModel.getStock());
        //将大闸的限制数字设到redis内
        redisTemplate.setKey("promo_door_count_"+promoId, itemModel.getStock() * 5);
    }

    /**
     * @description 将promo转换为PromoModel
     */
    private PromoModel convertFromEntity(Promo promo) {
        if (promo == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promo, promoModel);
        promoModel.setPromoItemPrice(BigDecimal.valueOf(promo.getPromoItemPrice()));
        promoModel.setStartTime(new DateTime(promo.getStartDate()));
        promoModel.setEndTime(new DateTime(promo.getEndDate()));
        return promoModel;
    }

    @Override
    public String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId) throws BusinessException {

        //判断是否库存已售罄，若对应的售罄key存在，则直接返回下单失败
        if(redisTemplate.hasKey("promo_item_stock_invalid_"+itemId)){
            return null;
        }
        Promo promoDO = promoMapper.selectByPrimaryKey(promoId);
        //dataobject->model
        PromoModel promoModel = convertFromEntity(promoDO);

        if(promoModel == null){
            return null;
        }
        //判断当前时间是否秒杀活动即将开始或正在进行
        if(promoModel.getStartTime().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndTime().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }
        //判断活动是否正在进行
        if(promoModel.getStatus() != 2){
            return null;
        }
        //判断item信息是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel == null){
            return null;
        }
        //判断用户信息是否存在
        UserModel userModel = userService.getUserByIdInCache(userId);
        if(userModel == null){
            return null;
        }

        //获取秒杀大闸的count数量
        long result = redisTemplate.increment("promo_door_count_"+promoId,-1);
        if(result < 0){
            return null;
        }
        //生成token并且存入redis内并给一个5分钟的有效期
        String token = UUID.randomUUID().toString().replace("-","");
        redisTemplate.setKey("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId,token);
        redisTemplate.expire("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId,5, TimeUnit.MINUTES);
        return token;
    }
}
