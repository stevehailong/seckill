package com.Long;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: Dubbo
 * @description: 商品服务启动类
 * @author: 寒风
 * @create: 2021-08-07 08:51
 **/
@EnableDubbo(scanBasePackages = "com.Long")
@SpringBootApplication
@MapperScan(basePackages = "com.Long.dao")
public class ItemService {
    public static void main(String[] args) {
        SpringApplication.run(ItemService.class, args);
    }
}
