package com.Long.Error;

/**
 * @program: Seckill
 * @description:
 * @author: 寒风
 * @create: 2021-03-18 14:08
 **/
public enum EmBusinessError implements CommonError {

    //通用的错误类型 以10000开头
    PARAMETER_VALIDATION_ERROR(10001,"参数不合法！"),
    UNKNOWN_ERROR(10002,"未知错误！"),

    //定义20000开头为用户模块相关错误定义(用户模块)
    USER_NOT_EXIST(20001,"用户不存在"),
    USER_LOGIN_FAIL(20002,"用户手机号或密码不正确"),
    USER_NOT_LOGIN(20003, "用户还未登录,即将跳转到登录界面！"),

    //定义30000开头为订单模块相关错误定义(用户模块)
    STOCK_NOT_ENOUGH(30001,"库存数量不足！"),
    MQ_SET_FAIL(30002,"库存异步消息失败！"),
    RATELIMIT(30003,"流量过大，请稍后访问！");

    EmBusinessError(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private int errCode; //错误码
    private String errMsg; //错误消息

    @Override
    public int getErrorCode() {
        return this.errCode;
    }

    @Override
    public String getErrorMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrorMsg(String errMsg) {
        this.errMsg =errMsg;
        return this;  //返回自己，定制修改通用错误码
    }
}
