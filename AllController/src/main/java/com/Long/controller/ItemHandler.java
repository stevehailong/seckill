package com.Long.controller;

import com.Long.Error.BusinessException;
import com.Long.Model.ItemModel;
import com.Long.Response.CommonReturnType;
import com.Long.ViewObject.ItemVO;
import com.Long.service.ItemService;
import com.Long.service.PromoService;
import com.Long.service.RedisServive;
import com.alibaba.dubbo.config.annotation.Reference;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @program: Seckill
 * @description: 商品模块的跳转URL
 * @author: 寒风
 **/
@RestController
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*") // 解决跨域问题
public class ItemHandler extends BaseHandler {
    @Reference
    private ItemService itemService;
    @Reference
    private RedisServive redisTemplate;
    @Reference
    private PromoService promoService;

    /**
     * @description 创建商品
     */
    @PostMapping(value = "/create", consumes = CONTENT_TYPE_FORMED)
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "imgUrl") String imgUrl,
                                       @RequestParam(name = "stock") Integer stock) throws BusinessException {
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setPrice(price);
        itemModel.setDescription(description);
        itemModel.setImgUrl(imgUrl);
        itemModel.setStock(stock);
        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);
        return CommonReturnType.create(itemVO);
    }
    /**
     * @description 将itemModel转换为ItemVO
     */
    private ItemVO convertVOFromModel(ItemModel itemModel) {
        if (itemModel == null) return null;
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        // 判断秒杀活动状态，是否聚合秒杀活动信息
        if (itemModel.getPromoModel() != null) {
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            itemVO.setStartTime(simpleDateFormat.format(itemModel.getPromoModel().getStartTime()));
        }else {
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }

    /**
     * @description 显示商品列表
     */
    @GetMapping(value = "/list")
    public CommonReturnType listItem() {
        List<ItemModel> itemModelList = itemService.listItem();
        // 使用Java8的stream流进行遍历赋值
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = this.convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }

    /**
     * @description 获取商品详情
     */
    @GetMapping(value = "/get")
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) {
        ItemModel itemModel = null;
        //先取本地缓存
        itemModel = (ItemModel)redisTemplate.getFromCommonCache("item_"+id);
        if(itemModel == null){
            //根据商品的id到redis内获取
            itemModel = (ItemModel) redisTemplate.getKey("item_"+id);
            //若redis内不存在对应的itemModel,则访问下游service
            if(itemModel == null){
                itemModel = itemService.getItemById(id);
                System.out.println(itemModel);
                //设置itemModel到redis内
                redisTemplate.setKey("item_"+id,itemModel);
                redisTemplate.expire("item_"+id,10, TimeUnit.MINUTES);
            }
            //填充本地缓存
            redisTemplate.setCommonCache("item_"+id,itemModel);
        }
        ItemVO itemVO = this.convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    // 发布秒杀活动
    @RequestMapping(value = "/publishpromo",method = {RequestMethod.GET})
    public CommonReturnType publishPromo(@RequestParam("id") Integer id){
        promoService.publishPromo(id);
        return CommonReturnType.create(null);
    }

}
