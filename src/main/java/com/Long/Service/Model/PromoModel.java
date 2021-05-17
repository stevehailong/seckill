package com.Long.Service.Model;

import lombok.Data;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @program: Seckill
 * @description: 秒杀模块的领域模型
 * @author: 寒风
 * @create: 2021-03-29 06:39
 **/
@Data
public class PromoModel {
    private Integer id;

    /**
     * 名称
     */
    private String promoName;

    /**
     * 活动状态:1表示未开始，2表示进行中，3表示已结束
     */
    private Integer status;

    /**
     * 开始时间
     */
    private DateTime startTime;

    /**
     * 结束时间
     */
    private DateTime endTime;

    /**
     * 适用商品
     */
    private Integer itemId;

    /**
     * 商品价格
     */
    private BigDecimal promoItemPrice;

}
