package com.Long;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: Dubbo
 * @description:
 * @author: 寒风
 * @create: 2021-08-06 22:18
 **/
@EnableDubbo(scanBasePackages = "com.Long") // 开启dubbo的注解支持
@SpringBootApplication
//@EnableHystrix // 开启服务容错注解支持
public class AllController {
    public static void main(String[] args) {
        SpringApplication.run(AllController.class, args);
    }

}

