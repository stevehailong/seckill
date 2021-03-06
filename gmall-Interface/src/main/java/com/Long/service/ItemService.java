package com.Long.service;

import com.Long.Error.BusinessException;
import com.Long.Model.ItemModel;

import java.util.List;

/**
 * @program: Seckill
 * @description: 商品操作的业务逻辑
 * @author: 寒风
 * @create: 2021-03-28 08:56
 **/
public interface ItemService {

    // 创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    // 商品列表浏览
    List<ItemModel> listItem();

    // 商品详情浏览
    ItemModel getItemById(Integer id) throws BusinessException;

    // 缓存获取商品信息
    ItemModel getItemByIdInCache(Integer id) throws BusinessException;

    // 普通商品库存扣减
    boolean decreaseSimpleStock(Integer itemId, Integer amount,Integer stock) throws BusinessException;

    // 回滚库存
    boolean increaseStock(Integer itemId, Integer amount);

    // 普通商品回滚库存
    boolean increaseSimpleStock(Integer itemId, Integer amount);

    // 库存扣减
    boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException;

    // 增加商品销量
    void increaseSales(Integer itemId, Integer amount) throws BusinessException;

    //初始化库存流水
    String initStockLog(Integer itemId, Integer amount);
}
