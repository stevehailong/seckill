package com.Long.Controller.ViewObject;

import lombok.Data;
import java.math.BigDecimal;

/**
 * @program: Seckill
 * @description: 前端需要展示的VO映射，可扩展性强，安全性高
 * @author: 寒风
 * @create: 2021-03-28 09:27
 **/
@Data
public class ItemVO {
    // 商品id
    private Integer id;

    // 商品标题
    private String title;

    // 商品价格 Double会有精度问题
    private BigDecimal price;

    // 商品库存
    private Integer stock;

    // 商品描述
    private String description;

    // 销量
    private Integer sales;

    // 图片
    private String imgUrl;

    /**
     * 记录商品是否在秒杀活动中，0：没有活动，1：待开始，2：正在进行
     */
    private Integer promoStatus;

    private BigDecimal promoPrice;

    private Integer promoId;

    // 倒计时展示用，用String便于传JSON数据到前端
    private String startTime;
}
