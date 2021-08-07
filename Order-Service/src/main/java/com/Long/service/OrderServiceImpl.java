package com.Long.service;

import com.Long.Error.BusinessException;
import com.Long.Error.EmBusinessError;
import com.Long.Model.ItemModel;
import com.Long.Model.OrderModel;
import com.Long.dao.OrderMapper;
import com.Long.dao.SequenceMapper;
import com.Long.dao.stockLogMapper;
import com.Long.entity.Order;
import com.Long.entity.Sequence;
import com.Long.entity.stockLog;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @program: Seckill
 * @description: 订单信息业务逻辑实现
 * @author: 寒风
 * @create: 2021-03-28 16:47
 **/
@Service
@com.alibaba.dubbo.config.annotation.Service(interfaceClass = com.Long.service.OrderService.class)
public class OrderServiceImpl implements OrderService {

    @Reference(timeout = 10000)
    private ItemService itemService;

    @Reference(timeout = 10000)
    private UserService userService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SequenceMapper sequenceMapper;

    @Autowired
    private stockLogMapper stockLogMapper;

    /**
     * @description 1、前端url上传递秒杀活动id，下单接口内校验对应id是否属于对应商品且活动已开始（使用）
     *              2、直接在下单接口内判断对应商品是否存在秒杀活动，若存在则以秒杀价格下单（会检验两次是否是秒杀商品）
     */
    @Override
    @Transactional // 涉及到库存加减，开启事务保证数据一致性
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String stockLogId) throws BusinessException {
        //校验下单状态，商品是否存在，用户是否合法，数量是否正确,活动信息
        ItemModel itemModel = itemService.getItemByIdInCache(itemId); // 减少对mysql的依赖
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品信息不存在");
        }
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "购买数量不正确");
        }
        //落单减库存 用户会拍下不付款 支付减库存异造成超卖问题
        boolean result = itemService.decreaseStock(itemId, amount);
        if (!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if (promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        //生成交易流水号（订单号）
        orderModel.setId(this.generateOrderNo());
        Order order = convertFromModel(orderModel);
        orderMapper.insertSelective(order);
        //增加商品销量
        itemService.increaseSales(itemId,amount);
        //设置库存流水状态为成功
        stockLog stockLogDO = stockLogMapper.selectByPrimaryKey(stockLogId);
        if(stockLogDO == null){
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }
        stockLogDO.setStatus(2);
        stockLogMapper.updateByPrimaryKeySelective(stockLogDO);
        //返回前端
        return orderModel;
    }

    /**
     * @description 将orderModel转换为Order
     */
    private Order convertFromModel(OrderModel orderModel) {
        if (orderModel == null) return null;
        Order order = new Order();
        BeanUtils.copyProperties(orderModel, order);
        order.setItemPrice(orderModel.getItemPrice().doubleValue());
        order.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return order;
    }

    /**
     * @description 生成订单号(如果事务createorder下单如果回滚，该下单方法中获得流水号id回滚，使等到的id号可能再一次被使用)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 外部的事务无论成功或失败，当前事务都必须成功
    String generateOrderNo() {
        //订单号16位
        //前8位为时间信息
        StringBuilder stringBuilder = new StringBuilder();
        // 根据当前日期获取前8为字符串
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);
        //中间6位为自增序列
        int seq = 0;
        Sequence sequence = sequenceMapper.getSequenceByName("order_info");
        seq = sequence.getCurrentValue();
        // 每次currentValue更新为步长
        sequence.setCurrentValue(sequence.getCurrentValue() + sequence.getStep());
        sequenceMapper.updateByPrimaryKeySelective(sequence);
        String seqStr = String.valueOf(seq);
        // 字符串将不足6为的字符串0补齐
        for(int i = 0; i < 6-seqStr.length(); i++) {
            stringBuilder.append(0);
        }
        // 添加到字符串末位
        stringBuilder.append(seqStr);
        //最后2位为分库分表位，暂时写死
        stringBuilder.append("00");
        return stringBuilder.toString();
    }
}
