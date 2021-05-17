package com.Long.Error;

/**
 * @program: Seckill
 * @description: 通用错误接口，处理返回错误信息，避免返回500等恶心的错误码
 * @author: 寒风
 * @create: 2021-03-18 14:04
 **/
public interface CommonError {
    int getErrorCode();  //错误码
    String getErrorMsg();  //错误信息
    CommonError setErrorMsg(String errMsg); //自定义错误信息
}
