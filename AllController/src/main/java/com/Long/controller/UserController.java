package com.Long.controller;

import com.Long.Error.BusinessException;
import com.Long.Error.EmBusinessError;
import com.Long.Model.UserModel;
import com.Long.Response.CommonReturnType;
import com.Long.ViewObject.UserVo;
import com.Long.service.RedisServive;
import com.Long.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @program: Dubbo
 * @description:
 * @author: 寒风
 **/

@RestController
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController  extends BaseHandler{

    static final String PATTEN_REGEX_PHONE = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";

    @Reference
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest; //不会发生并发问题

    @Reference
    private RedisServive redisServive;

    @RequestMapping("/get/{id}")
    public CommonReturnType getUser(@PathVariable("id") Integer id) throws BusinessException {
        //调用Service层获取对应的用户对象返回给前端
        UserModel userModel = userService.getUserById(id);
        //若用户不存在，抛出自定义的异常
        if (userModel == null) throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        //将核心业务模型转换为UI模型
        UserVo userVo = convert(userModel);
        //返回通用对象
        return CommonReturnType.create(userVo);
    }
    private UserVo convert(UserModel userModel) {
        if (userModel == null) return null;
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userModel, userVo);
        return userVo;
    }

    //手机验证注册
    @PostMapping(value = "/getOtp", consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType getOtp(@RequestParam(name = "telphone")String telphone) throws BusinessException {
        if (!telphone.matches(PATTEN_REGEX_PHONE)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "请填写正确格式的手机号");
        }
        //需要按照一定的规则生成opt验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);
        //将otp验证码同手机的手机号进行关联(暂时使用session方式进行关联)
        httpServletRequest.getSession().setAttribute(telphone, otpCode);
        //将otp验证码通过短信通道发送给用户(模拟)
        System.out.println("telphone = " + telphone + "  otpCode = " + otpCode); //打印到控制台上,泄漏了隐私
        return CommonReturnType.create(null);
    }

    //用户注册
//    @Transactional //涉及到插入和更新操作，需开启事务
    @PostMapping(value = "/register", consumes = CONTENT_TYPE_FORMED)
    public CommonReturnType register(@RequestParam("telphone") String telphone,
                                     @RequestParam("otpCode") String otpCode,
                                     @RequestParam("name") String name,
                                     @RequestParam("age") Integer age,
                                     @RequestParam("gender") Integer gender,
                                     @RequestParam("password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if (!telphone.matches(PATTEN_REGEX_PHONE)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "请填写正确格式的手机号");
        }
        //验证手机号和对应的otp验证码一致
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if (!StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "验证码输入有误");
        }
        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byPhone");
        userModel.setEncrptPassword(this.encodeByMD5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }
    private String encodeByMD5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // 确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        // 加密字符串
        String newStr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }


    @PostMapping(value = "/login", consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                  @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if (!telphone.matches(PATTEN_REGEX_PHONE)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "请填写正确格式的手机号");
        }
        if (StringUtils.isEmpty(telphone)||StringUtils.isEmpty(password)) throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        //调用登录服务
        UserModel userModel = userService.validateLogin(telphone,this.encodeByMD5(password));

        // 建立token和用户登录态之间的联系
        String uuidToken = UUID.randomUUID().toString();
        uuidToken = uuidToken.replace("-",""); // 去掉uuid的横线
        redisServive.setKey(uuidToken,userModel);
        redisServive.expire(uuidToken,1, TimeUnit.HOURS); // 设置1小时过期
        return CommonReturnType.create(uuidToken);
    }
}