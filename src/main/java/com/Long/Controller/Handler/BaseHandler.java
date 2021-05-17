package com.Long.Controller.Handler;


import com.Long.Controller.Response.CommonReturnType;
import com.Long.Error.BusinessException;
import com.Long.Error.EmBusinessError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;

/**
 * @program: Seckill
 * @description: 提出公共异常处理每个controller继承即可
 * @author: 寒风
 * @create: 2021-03-18 16:10
 **/
public class BaseHandler {
    public static final String CONTENT_TYPE_FORMED="application/x-www-form-urlencoded";
//    //定义exceptionHandler处理未被Handler层吸收的Exception
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.OK)  //修改状态码
//    @ResponseBody
//    public Object handlerException( Exception ex){ //此步只会找本地下的相关页面文件，还需处理
//        HashMap<String, Object> res = new HashMap<>();
//        if (ex instanceof BusinessException){
//            BusinessException Bex = (BusinessException) ex;
//            res.put("errCode",Bex.getErrorCode());
//            res.put("errMsg",Bex.getErrorMsg());
//        }else {
//            res.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrorCode());
//            res.put("errMsg",EmBusinessError.UNKNOWN_ERROR.getErrorMsg());
//        }
//        return CommonReturnType.create(res,"fail");
//    }
}
