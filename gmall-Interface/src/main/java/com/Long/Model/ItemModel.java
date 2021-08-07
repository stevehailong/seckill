package com.Long.Model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @program: Seckill
 * @description: Service层 商品领域模型
 * @author: 寒风
 * @create: 2021-03-28 08:40
 **/
@Data
public class ItemModel implements Serializable {
    // 商品id
    private Integer id;

    // 商品标题
    @NotNull(message = "商品名称不能为空")
    private String title;

    // 商品价格 Double会有精度问题
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "商品价格必须大于0")
    private BigDecimal price;

    // 商品库存
    @NotNull(message = "库存必须填写")
    private Integer stock;

    // 商品描述
    @NotNull(message = "商品描述不能为空")
    private String description;

    // 销量
    private Integer sales;

    // 图片
    @NotNull(message = "商品图片信息不能为空")
    private String imgUrl;

    /**
     * 使用聚合模型，如果promoModel不为空，则表示其拥有还未结束的秒杀活动
     */
    private PromoModel promoModel;

}
