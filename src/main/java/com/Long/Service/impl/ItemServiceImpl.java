package com.Long.Service.impl;

import com.Long.Dao.ItemMapper;
import com.Long.Dao.ItemStockMapper;
import com.Long.Dao.stockLogMapper;
import com.Long.Error.BusinessException;
import com.Long.Error.EmBusinessError;
import com.Long.Service.ItemService;
import com.Long.Service.Model.ItemModel;
import com.Long.Service.Model.PromoModel;
import com.Long.Service.PromoService;
import com.Long.entity.Item;
import com.Long.entity.ItemStock;
import com.Long.entity.stockLog;
import com.Long.mq.MqProducer;
import com.Long.validator.ValidationResult;
import com.Long.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @program: Seckill
 * @description: 业务逻辑接口的实现
 * @author: 寒风
 * @create: 2021-03-28 09:02
 **/
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemStockMapper itemStockMapper;

    @Autowired
    private stockLogMapper stockLogMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    /**
     * @description 创建商品
     */
    @Override
    @Transactional // 需提交事务保证原子性
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors())
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        //转化model
        Item item = this.convertItemFromItemModel(itemModel);
        //写入数据库
        itemMapper.insertSelective(item);
        itemModel.setId(item.getId());
        ItemStock itemStock = this.convertItemStockFromItemModel(itemModel);
        itemStockMapper.insertSelective(itemStock);
        //返回对象
        return this.getItemById(itemModel.getId());
    }

    /**
     * @description 将itemModel转换为Item
     */
    private Item convertItemFromItemModel(ItemModel itemModel) {
        if (itemModel == null) return null;
        Item item = new Item();
        BeanUtils.copyProperties(itemModel, item);
        //数据库中price是double类型的，ItemModel中是BigDecimal，避免类型转化时出现精度丢失
        item.setPrice(itemModel.getPrice().doubleValue());
        return item;
    }

    /**
     * @description 将itemModel转化为ItemStock
     */
    private ItemStock convertItemStockFromItemModel(ItemModel itemModel) {
        if (itemModel == null) return null;
        ItemStock itemStock = new ItemStock();
        itemStock.setItemId(itemModel.getId());
        itemStock.setStock(itemModel.getStock());
        return itemStock;
    }

    /**
     * @description 展示商品列表
     */
    @Override
    public List<ItemModel> listItem() {
        List<Item> itemList = itemMapper.listItem();
        // java8的stream流进行筛选
        List<ItemModel> itemModelList = itemList.stream().map(item -> {
            ItemStock itemStock = itemStockMapper.selectByItemId(item.getId());
            ItemModel itemModel = this.convertFromEntity(item, itemStock);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    /**
     * @description 通过id获取商品详情
     */
    @Override
    public ItemModel getItemById(Integer id) {
        Item item = itemMapper.selectByPrimaryKey(id);
        if (item == null) return null;
        // 获得库存数量
        ItemStock itemStock = itemStockMapper.selectByItemId(item.getId());
        // 返回Model对象
        ItemModel itemModel = convertFromEntity(item, itemStock);
        // 判断是否右秒杀活动
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if (promoModel != null && promoModel.getStatus() != 3) {
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    
    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validate_" + id);
        // 如果Redis中不存在就从数据库中取，并存入缓存中
        if (itemModel == null) {
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validate_" + id, itemModel);
            redisTemplate.expire("item_validate_" + id, 10, TimeUnit.MINUTES); // 10分钟的有效期
        }
        return itemModel;
    }

    // 异步更新库存
    @Override
    public boolean asyncDecreaseStock(Integer itemId, Integer amount) {
        return mqProducer.asyncReduceStock(itemId, amount);
    }

    @Override
    public boolean increaseStock(Integer itemId, Integer amount) {
        redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue());
        return true;
    }

    @Override
    @Transactional // 设计到减库存，开启事务 返回值的巧妙，不执行sql语句返回0
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
//        int affectedRow = itemStockMapper.decreaseStock(itemId, amount);
//        使用redis扣减库存
        long result = redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue() * -1);
        if (result > 0) {
            return true;
        } else if(result == 0){
            //打上库存已售罄的标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid_"+itemId,"true");
            //更新库存成功
            return true;
        }else {
            // 回滚redis的库存
            increaseStock(itemId,amount);
            return false;
        }
    }

    @Override // 增加销量，开启事务
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemMapper.increaseSales(itemId, amount);
    }

    //初始化对应的库存流水
    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {
        stockLog stockLogDO = new stockLog();
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));
        stockLogDO.setStatus(1);
        stockLogMapper.insertSelective(stockLogDO);
        return stockLogDO.getStockLogId();
    }
    /**
     * @description 将item和itemStock转换为ItemModel
     */
    private ItemModel convertFromEntity(Item item, ItemStock itemStock) {
        if (item == null) return null;
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(item, itemModel);
        //从数据库data向item model转的时候也要注意price的类型问题
        itemModel.setPrice(new BigDecimal(item.getPrice()));
        itemModel.setStock(itemStock.getStock());
        return itemModel;
    }
}
