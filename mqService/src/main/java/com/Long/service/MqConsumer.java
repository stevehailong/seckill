package com.Long.service;
import com.Long.dao.ItemStockMapper;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @program: Seckill
 * @description: 消息消费者
 * @author: 寒风
 **/
@Component
public class MqConsumer {

    private DefaultMQPushConsumer consumer;

    @Autowired
    private ItemStockMapper itemStockMapper;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicName}")
    private String topicName;

    @Reference(retries = 5)
    private RedisServive redisServive;

    @PostConstruct
    public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer("stock_consumer_group");
        consumer.setNamesrvAddr(nameAddr);
        // 订阅所有消息
        consumer.subscribe(topicName,"*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //实现库存真正到数据库内扣减的逻辑
                Message msg = list.get(0);
                String jsonString  = new String(msg.getBody());
                Map<String,Object> map = JSON.parseObject(jsonString, Map.class);
                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");
                itemStockMapper.decreaseStock(itemId,amount);
                // 下单成功删除缓存
                redisServive.delete("item_"+itemId);
                redisServive.deleteCash("item_"+itemId);
                redisServive.delete("item_validate_"+itemId);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }
}
