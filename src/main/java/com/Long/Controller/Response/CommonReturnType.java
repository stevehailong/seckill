package com.Long.Controller.Response;

import lombok.Data;

/**
 * @program: Seckill
 * @description: 为前端使用方便，提供规范化格式
 * @author: 寒风
 * @create: 2021-03-18 11:05
 **/
@Data
public class CommonReturnType {
    //表明对应的返回处理结果 “success” 或 “fail”
    private String status;
    //若status为“success”，则data类返回前端数据
    //若data内为"false",则使用通用的错误码返回
    private Object data;

    //定义一个通用的创建方法
    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result,"success");
    }

    //使用重载的方式处理
    public static CommonReturnType create(Object result,String status){
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }
}
