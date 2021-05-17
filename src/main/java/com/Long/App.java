package com.Long;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = {"com.Long"}) //启动化配置
@MapperScan("com.Long.Dao")
public class App 
{
    public static void main( String[] args ){
        SpringApplication.run(App.class,args);
    }
}
